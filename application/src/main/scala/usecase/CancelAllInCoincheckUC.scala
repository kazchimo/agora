package usecase

import domain.AllEnv
import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.CoincheckExchange
import zio.{RIO, ZIO}

object CancelAllInCoincheckUC {
  def cancelAll: RIO[AllEnv, Unit] = for {
    openOrders <- CoincheckExchange.openOrders
    broker      = CoincheckBroker()
    _          <- ZIO.foreachPar_(openOrders)(o => broker.cancelWithWait(o.id))
  } yield ()
}
