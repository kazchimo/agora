package usecase

import domain.exchange.coincheck.CoincheckExchange
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.logging.{Logging, log}
import zio.{ZEnv, ZIO}

object WatchCoincheckTransactionUC {
  def watch = for {
    _      <- log.info("Watching Coincheck transactions...")
    stream <- CoincheckExchange.publicTransactions
    _      <- stream.foreach(s => log.info(s))
    _      <- log.info("Finished watching Coincheck transactions")
  } yield ()
}
