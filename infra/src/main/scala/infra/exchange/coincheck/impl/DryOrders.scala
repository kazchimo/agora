package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.{CCOrder, CCOrderRequest}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{RIO, ZIO}
import zio.duration._

private[coincheck] trait DryOrders {
  self: DryCoincheckExchangeImpl =>
  val orderSettledInterval: Int

  final override def orders(
    order: CCOrderRequest
  ): RIO[SttpClient with zio.ZEnv with Conf, CCOrder] = for {
    order <- ZIO.succeed(cache.submitOrder)
    _     <- ZIO
               .succeed(cache.closeOrder(order.id)).delay(
                 orderSettledInterval.seconds
               ).fork
  } yield order
}
