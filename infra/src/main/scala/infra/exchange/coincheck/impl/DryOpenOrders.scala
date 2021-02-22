package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCOpenOrder
import zio.{RIO, ZIO}

private[coincheck] trait DryOpenOrders {
  self: DryCoincheckExchangeImpl =>

  final override def openOrders: RIO[AllEnv, Seq[CCOpenOrder]] =
    ZIO.succeed(fakeExchange.openOrders)
}
