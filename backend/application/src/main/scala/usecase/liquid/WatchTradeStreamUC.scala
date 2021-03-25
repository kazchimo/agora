package usecase.liquid

import domain.exchange.liquid.{LiquidExchange, Trade}
import lib.zio.EStream
import zio.logging.log

object WatchTradeStreamUC {
  def watch = for {
    str: EStream[Trade] <- LiquidExchange.tradesStream
    _                   <- str.foreach(t => log.info(t.toString))
  } yield ()
}
