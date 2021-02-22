package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCOrder.CCOrderType
import domain.exchange.coincheck.{CCOrder, CCOrderRequest}
import zio.duration._
import zio.{RIO, ZIO}

private[coincheck] trait DryOrders {
  self: DryCoincheckExchangeImpl =>
  val orderSettledInterval: Int

  final override def orders(
    order: CCOrderRequest[_ <: CCOrderType]
  ): RIO[AllEnv, CCOrder] = for {
    order <- ZIO.succeed(fakeExchange.submitOrder(order))
    _     <-
      fakeExchange.closeOrder(order.id).delay(orderSettledInterval.seconds).fork
  } yield order
}
