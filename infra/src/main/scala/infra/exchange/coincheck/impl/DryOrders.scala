package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderType
import domain.exchange.coincheck.{CCOrder, CCOrderRequest}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.duration._
import zio.{RIO, ZIO}

private[coincheck] trait DryOrders {
  self: DryCoincheckExchangeImpl =>
  val orderSettledInterval: Int

  final override def orders(
    order: CCOrderRequest[_ <: CCOrderType]
  ): RIO[SttpClient with zio.ZEnv with Conf, CCOrder] = for {
    order <- ZIO.succeed(fakeExchange.submitOrder(order))
    _     <-
      fakeExchange.closeOrder(order.id).delay(orderSettledInterval.seconds).fork
  } yield order
}
