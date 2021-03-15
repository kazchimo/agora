package usecase

import cats.Show
import cats.syntax.show._
import domain.AllEnv
import domain.exchange.coincheck.{CCPublicTransaction, CoincheckExchange}
import lib.syntax.all._
import zio.RIO
import zio.logging.log

object WatchCoincheckTransactionUC {
  implicit private val show: Show[CCPublicTransaction] = Show.show(a => s"""
      |id: ${a.id.value.toString}
      |pair: ${a.pair.deepInnerV}
      |rate: ${a.rate.value.toString}
      |quantity: ${a.quantity.value.toString}
      |side: ${a.side.entryName}
      |""".stripMargin)

  def watch: RIO[AllEnv, Unit] = for {
    _      <- log.info("Watching Coincheck transactions...")
    stream <- CoincheckExchange.publicTransactions
    _      <- stream.foreach(s => log.info(s.show))
    _      <- log.info("Finished watching Coincheck transactions")
  } yield ()
}
