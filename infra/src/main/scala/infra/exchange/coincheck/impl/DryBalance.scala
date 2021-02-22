package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCBalance
import zio.RIO

private[coincheck] trait DryBalance {
  self: DryCoincheckExchangeImpl =>

  final override def balance: RIO[AllEnv, CCBalance] =
    CCBalance.fromRaw(fakeExchange.jpy, fakeExchange.btc)
}
