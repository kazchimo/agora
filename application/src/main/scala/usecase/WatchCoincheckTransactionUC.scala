package usecase

import domain.exchange.coincheck.CoincheckExchange
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.ZIO
import zio.logging.{Logging, log}

object WatchCoincheckTransactionUC {
  def watch: ZIO[
    CoincheckExchange with zio.ZEnv with Logging with SttpClient,
    Throwable,
    Unit
  ] = for {
    _      <- log.info("Watching Coincheck transactions...")
    stream <- CoincheckExchange.publicTransactions
    _      <- stream.foreach(s => log.info(s))
    _      <- log.info("Finished watching Coincheck transactions")
  } yield ()
}
