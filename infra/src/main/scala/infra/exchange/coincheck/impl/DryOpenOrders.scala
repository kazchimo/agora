package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{RIO, ZIO}

private[coincheck] trait DryOpenOrders {
  self: DryCoincheckExchangeImpl =>

  final override def openOrders: RIO[SttpClient with Conf, Seq[CCOrder]] =
    ZIO.succeed(fakeExchange.openOrders)
}
