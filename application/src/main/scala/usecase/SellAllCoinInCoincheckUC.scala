package usecase

import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.{CCOrderRequest, CoincheckExchange}
import lib.error.AdaptorInternalError
import zio.ZIO
import zio.logging.log

object SellAllCoinInCoincheckUC {
  def sell(updatePriceIntervalSec: Int) = for {
    balanceRef              <- CoincheckExchange.balance.fork
    transactionsRef         <- CoincheckExchange.transactions.fork
    (balance, transactions) <- balanceRef.zip(transactionsRef).join
    transaction             <- ZIO
                                 .fromOption(transactions.headOption).orElseFail(
                                   AdaptorInternalError("Could not find latest price!")
                                 )
    req                     <- CCOrderRequest
                                 .limitSell(transaction.rate.value.value, balance.btc.value.value)
    _                       <- log.info(s"Sell All btc at ${req.toString}!")
    _                       <- CoincheckBroker().priceAdjustingOrder(req, updatePriceIntervalSec)
  } yield ()
}
