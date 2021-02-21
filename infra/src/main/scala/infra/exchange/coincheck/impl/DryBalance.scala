package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.coincheck.CCBalance
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.RIO
import zio.logging.Logging

private[coincheck] trait DryBalance extends AuthStrategy {
  self: DryCoincheckExchangeImpl =>

  final override def balance
    : RIO[SttpClient with Conf with Logging, CCBalance] =
    CCBalance.fromRaw(fakeExchange.jpy, fakeExchange.btc)
}
