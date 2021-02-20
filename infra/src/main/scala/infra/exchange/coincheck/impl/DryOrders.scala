package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.{CCOrder, CCOrderRequest}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{RIO, ZIO}

private[coincheck] trait DryOrders {
  self: DryCoincheckExchangeImpl =>

  final override def orders(
    order: CCOrderRequest
  ): RIO[SttpClient with zio.ZEnv with Conf, CCOrder] =
    ZIO.succeed(cache.submitOrder)
}
