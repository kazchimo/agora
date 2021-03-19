package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidCurrencyPairCode.BtcJpy
import domain.exchange.liquid.LiquidOrder._
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import lib.error.ClientDomainError
import sttp.ws.WebSocket
import zio.logging.log
import zio.{IO, Queue, RIO, Task, ZIO, stream}
import zio.stream._
import cats.syntax.traverse._
import domain.exchange.liquid.LiquidExchange.OrderSide
import infra.exchange.liquid.impl.OrdersStream.toLiquidOrders
import zio.interop.catz.core._

private[liquid] trait OrdersStream extends WebSocketHandler {
  self: LiquidExchange.Service =>
  private def useWS(queue: Queue[Seq[LiquidOrder]], side: OrderSide)(
    ws: WebSocket[RIO[AllEnv, *]]
  ) = handleMessage(
    ws,
    s"price_ladders_cash_${BtcJpy.entryName}_${side.entryName}",
    "updated"
  )(d => toLiquidOrders(d.data).flatMap(queue.offer).unit).forever

  override def ordersStream(
    side: OrderSide
  ): RIO[AllEnv, stream.Stream[Throwable, Seq[LiquidOrder]]] = for {
    queue <- Queue.unbounded[Seq[LiquidOrder]]
    fiber <- sendWS(useWS(queue, side)).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}

object OrdersStream {
  private val pricesRegex = raw"""\["([\d\.]*)","([\d\.]*)"\]""".r

  def toLiquidOrders(d: String): IO[ClientDomainError, List[LiquidOrder]] =
    pricesRegex
      .findAllMatchIn(d).map { r =>
        for {
          p <- Price(r.group(1).toDouble)
          q <- Quantity(r.group(2).toDouble)
        } yield LiquidOrder(p, q)
      }.toList.sequence
}
