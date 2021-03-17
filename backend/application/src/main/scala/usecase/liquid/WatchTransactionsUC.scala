package usecase.liquid

import cats.Show
import cats.syntax.show._
import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import lib.syntax.all._
import zio.ZIO
import zio.logging.log

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
