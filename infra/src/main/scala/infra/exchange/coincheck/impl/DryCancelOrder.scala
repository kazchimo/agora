package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCOrder
import zio.{RIO, ZIO}

private[coincheck] trait DryCancelOrder {
  self: DryCoincheckExchangeImpl =>

  final override def cancelOrder(
    id: CCOrder.CCOrderId
  ): RIO[AllEnv, CCOrder.CCOrderId] = ZIO.succeed {
    fakeExchange.cancelOrder(id)
    id
  }
}
