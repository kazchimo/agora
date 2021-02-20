package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{RIO, ZIO}

private[coincheck] trait DryCancelStatus {
  self: DryCoincheckExchangeImpl =>

  final override def cancelStatus(
    id: CCOrder.CCOrderId
  ): RIO[SttpClient with Conf, Boolean] = ZIO.succeed(!cache.hasId(id))
}
