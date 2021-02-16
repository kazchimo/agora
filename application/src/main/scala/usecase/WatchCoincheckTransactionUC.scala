package usecase

import domain.exchange.coincheck.CoincheckExchange
import zio.logging.log
import zio.Has

object WatchCoincheckTransactionUC {
  def watch: ZIO[Has[CoincheckExchange.Service] with SttpClient with ZEnv with Logging,Throwable,Unit] = for {
    _      <- log.info("Watching Coincheck transactions...")
    stream <- CoincheckExchange.publicTransactions
    _      <- stream.foreach(s => log.info(s))
    _      <- log.info("Finished watching Coincheck transactions")
  } yield ()
}
