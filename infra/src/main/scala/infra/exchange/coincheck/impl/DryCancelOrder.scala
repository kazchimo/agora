package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.logging.Logging
import zio.{RIO, ZIO}

private[coincheck] trait DryCancelOrder {
  self: DryCoincheckExchangeImpl =>

  final override def cancelOrder(
    id: CCOrder.CCOrderId
  ): RIO[SttpClient with Logging with Conf, CCOrder.CCOrderId] = ZIO.succeed {
    cache.cancelOrder(id)
    id
  }
}
