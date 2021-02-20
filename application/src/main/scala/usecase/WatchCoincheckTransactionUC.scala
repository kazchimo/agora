package usecase

import cats.Show
import cats.syntax.show._
import domain.conf.Conf
import domain.exchange.coincheck.{CCPublicTransaction, CoincheckExchange}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.ZIO
import zio.logging.{Logging, log}

object WatchCoincheckTransactionUC {
  implicit private val show: Show[CCPublicTransaction] = Show.show(a => s"""
      |id: ${a.id.value.toString}
      |pair: ${a.pair.value.value}
      |rate: ${a.rate.value.toString}
      |quantity: ${a.quantity.value.toString}
      |side: ${a.side.entryName}
      |""".stripMargin)

  def watch: ZIO[
    CoincheckExchange with zio.ZEnv with Logging with SttpClient with Conf,
    Throwable,
    Unit
  ] = for {
    _      <- log.info("Watching Coincheck transactions...")
    stream <- CoincheckExchange.publicTransactions
    _      <- stream.foreach(s => log.info(s.show))
    _      <- log.info("Finished watching Coincheck transactions")
  } yield ()
}
