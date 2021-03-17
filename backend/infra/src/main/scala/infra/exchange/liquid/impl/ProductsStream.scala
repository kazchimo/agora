package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import infra.exchange.liquid.Endpoints
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.refined._
import io.circe.syntax._
import lib.error.InternalInfraError
import lib.refined.NonNegativeDouble
import sttp.client3.asynchttpclient.zio.sendR
import sttp.client3.{basicRequest, _}
import sttp.ws.{WebSocket, WebSocketClosed, WebSocketFrame}
import zio.logging.log
import zio.stream.Stream
import zio.{Queue, RIO, ZIO}

private[liquid] case class ProductResponse(
  last_traded_price: NonNegativeDouble,
  last_traded_quantity: NonNegativeDouble
) {
  def toLiquidProduct: LiquidProduct = LiquidProduct(
    LiquidProduct.LastTradedPrice(last_traded_price),
    LiquidProduct.LastTradedQuantity(last_traded_quantity)
  )
}

private[liquid] case class WSMessage(
  event: String,
  data: Option[ProductResponse]
)

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
  )(ws: WebSocket[RIO[AllEnv, *]]) = log.debug("Websocket start!") *> (for {
    msg  <- ws.receiveTextFrame()
    _    <- log.trace(msg.payload)
    data <- ZIO.fromEither(decode[WSMessage](msg.payload))
    _    <- data.event match {
              case "pusher:connection_established"          =>
                ws.send(subscribeText) *> log.info("Connected!")
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case "updated"                                => ZIO
                  .getOrFailWith(
                    InternalInfraError(
                      s"No content updated ws message: ${data.toString}"
                    )
                  )(data.data)
                  .flatMap(d => queue.offer(d.toLiquidProduct))
              case a                                        => log.warn(s"Unexpected ws response: $a")
            }
  } yield ()).retryWhile(_.isInstanceOf[WebSocketClosed]).forever

  final override def productsStream
    : ZIO[AllEnv, Nothing, Stream[Throwable, LiquidProduct]] = for {
    _     <- log.info("Getting liquid product stream...")
    queue <- Queue.unbounded[LiquidProduct]
    fiber <-
      sendR[Unit, AllEnv](
        basicRequest
          .get(uri"${Endpoints.ws}").response(asWebSocketAlways(useWS(queue)))
      ).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}
