package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCOpenOrder
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{RIO, ZIO}

private[coincheck] trait DryOpenOrders {
  self: DryCoincheckExchangeImpl =>

  final override def openOrders: RIO[SttpClient with Conf, Seq[CCOpenOrder]] =
    ZIO.succeed(fakeExchange.openOrders)
}
