package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.refined._
import lib.refined.NonNegativeDouble
import sttp.ws.WebSocket
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

private[liquid] trait ProductsStream extends WebSocketHandler {
  self: LiquidExchange.Service =>

  private def useWS(
    queue: Queue[LiquidProduct]
  )(ws: WebSocket[RIO[AllEnv, *]]) = log.debug(
    "Websocket start!"
  ) *> handleMessage(ws, "product_cash_btcjpy_5", "updated")(s =>
    for {
      d   <- ZIO.fromEither(decode[DataMessage](s))
      res <- ZIO.fromEither(decode[ProductResponse](d.data))
      _   <- queue.offer(res.toLiquidProduct)
    } yield ()
  ).forever

  final override def productsStream
    : ZIO[AllEnv, Nothing, Stream[Throwable, LiquidProduct]] = for {
    _     <- log.info("Getting liquid product stream...")
    queue <- Queue.unbounded[LiquidProduct]
    fiber <- sendWS(useWS(queue)).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}
