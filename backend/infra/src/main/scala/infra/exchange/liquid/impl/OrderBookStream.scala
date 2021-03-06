package infra.exchange.liquid.impl

import cats.syntax.traverse._
import domain.AllEnv
import domain.exchange.liquid.LiquidCurrencyPairCode.BtcJpy
import domain.exchange.liquid.LiquidOrder._
import domain.exchange.liquid.{LiquidExchange, OrderOnBook}
import infra.exchange.liquid.impl.OrderBookStream.toLiquidOrders
import lib.error.ClientDomainError
import sttp.ws.WebSocket
import zio._
import zio.interop.catz.core._
import zio.stream._

private[liquid] trait OrderBookStream extends WebSocketHandler {
  self: LiquidExchange.Service =>
  private def useWS(queue: Queue[Seq[OrderOnBook]], side: Side)(
    ws: WebSocket[RIO[AllEnv, *]]
  ) = handleMessage(
    ws,
    s"price_ladders_cash_${BtcJpy.entryName}_${side.entryName}",
    "updated"
  )(d => toLiquidOrders(d.data).flatMap(queue.offer).unit).forever

  override def orderBookStream(
    side: Side
  ): RIO[AllEnv, stream.Stream[Throwable, Seq[OrderOnBook]]] = for {
    queue <- Queue.unbounded[Seq[OrderOnBook]]
    fiber <- sendWS(useWS(queue, side)).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}

object OrderBookStream {
  private val pricesRegex = raw"""\["([\d\.]*)","([\d\.]*)"\]""".r

  def toLiquidOrders(d: String): IO[ClientDomainError, List[OrderOnBook]] =
    pricesRegex
      .findAllMatchIn(d).map { r =>
        ZIO.mapN(Price(r.group(1).toDouble), Quantity(r.group(2).toDouble))(
          OrderOnBook.apply
        )
      }.toList.sequence
}
