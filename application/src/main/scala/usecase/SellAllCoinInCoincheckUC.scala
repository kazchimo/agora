package usecase

import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.{CCOrderRequest, CoincheckExchange}
import lib.error.AdaptorInternalError
import zio.ZIO
import zio.logging.log
import lib.syntax.all._

object SellAllCoinInCoincheckUC {
  def sell(updatePriceIntervalSec: Int) = for {
    balanceFiber            <- CoincheckExchange.balance.fork
    transactionsFiber       <- CoincheckExchange.transactions.fork
    (balance, transactions) <- balanceFiber.zip(transactionsFiber).join
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
