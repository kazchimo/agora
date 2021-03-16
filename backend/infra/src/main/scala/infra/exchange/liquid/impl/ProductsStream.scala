package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import infra.exchange.liquid.Endpoints
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.refined._
import lib.error.InternalInfraError
import lib.refined.NonNegativeDouble
import sttp.client3.asynchttpclient.zio.sendR
import sttp.client3.{basicRequest, _}
import sttp.ws.{WebSocket, WebSocketClosed, WebSocketFrame}
import zio.logging.log
import zio.stream.UStream
import zio.{Queue, RIO, ZIO}
import zio.stream.Stream

private[liquid] case class ProductResponse(
  last_traded_price: NonNegativeDouble,
  last_traded_quantity: NonNegativeDouble
) {
  def toLiquidProduct: LiquidProduct = LiquidProduct(
    LiquidProduct.LastTradedPrice(last_traded_price),
    LiquidProduct.LastTradedQuantity(last_traded_quantity)
  )
}

private[liquid] case class WSData(event: String, data: Option[ProductResponse])

private[liquid] trait ProductsStream { self: LiquidExchange.Service =>
  private val subscribeText = WebSocketFrame.text(
    Json
      .obj(
        "event" -> "pusher:subscribe".asJson,
        "data"  -> Json.obj("channel" -> "product_cash_BTC/JPY_5".asJson)
      ).asJson.noSpaces
  )

  private def useWS(
    queue: Queue[LiquidProduct]
  )(ws: WebSocket[RIO[AllEnv, *]]) = (for {
    msg  <- ws.receiveTextFrame()
    data <- ZIO.fromEither(decode[WSData](msg.payload))
    _    <- data.event match {
              case "pusher:connection_established"          => ws.send(subscribeText)
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case "updated"                                => ZIO
                  .getOrFailWith(
                    InternalInfraError(
                      s"No content updated ws message: ${data.toString}"
                    )
                  )(data.data)
                  .flatMap(d => queue.offer(d.toLiquidProduct))
            }
  } yield ()).retryWhile(_.isInstanceOf[WebSocketClosed]).forever

  final override def productsStream
    : ZIO[AllEnv, Throwable, UStream[LiquidProduct]] = for {
    queue <- Queue.unbounded[LiquidProduct]
    _     <-
      sendR[Unit, AllEnv](
        basicRequest
          .get(uri"${Endpoints.ws}").response(asWebSocketAlways(useWS(queue)))
      ).fork
  } yield Stream.fromQueueWithShutdown(queue).haltWhen(queue.awaitShutdown)
}
