package usecase.liquid

import cats.Show
import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import zio.{Has, ZIO}
import zio.logging.log
import cats.syntax.show._
import lib.syntax.all._

object WatchTransactionsUC {
  implicit private val show: Show[LiquidProduct] = Show.show { a =>
    s"""
       |last_traded_price: ${a.lastTradedPrice.deepInnerV.toString}
       |last_traded_quantity: ${a.lastTradedQuantity.deepInnerV.toString}
       |""".stripMargin

  }

  def watch: ZIO[AllEnv, Throwable, Unit] = for {
    _      <- log.info("Watching liquid transactions...")
    stream <- LiquidExchange.productsStream
    _      <- stream.foreach(s => log.info(s.show))
  } yield ()
}
