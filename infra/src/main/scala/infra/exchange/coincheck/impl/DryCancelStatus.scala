package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCOrder
import zio.{RIO, ZIO}

private[coincheck] trait DryCancelStatus {
  self: DryCoincheckExchangeImpl =>

  final override def cancelStatus(id: CCOrder.CCOrderId): RIO[AllEnv, Boolean] =
    ZIO.succeed(!fakeExchange.submitted(id))
}
