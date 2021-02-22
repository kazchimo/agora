package usecase

import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.{CCOrderRequest, CoincheckExchange}
import lib.error.AdaptorInternalError
import lib.syntax.all._
import zio.ZIO
import zio.logging.log

object SellAllCoinInCoincheckUC {
  def sell(updatePriceIntervalSec: Int) = for {
    (balance, transactions) <-
      CoincheckExchange.balance.zipPar(CoincheckExchange.transactions)
    transaction             <- ZIO
                                 .fromOption(transactions.headOption).orElseFail(
                                   AdaptorInternalError("Could not find latest price!")
                                 )
    req                     <- CCOrderRequest
                                 .limitSell(transaction.rate.deepInnerV, balance.btc.deepInnerV)
    _                       <- log.info(s"Sell All btc at ${req.toString}!")
    _                       <- CoincheckBroker().priceAdjustingOrder(req, updatePriceIntervalSec)
  } yield ()
}
