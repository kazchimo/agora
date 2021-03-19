package usecase.liquid

import domain.exchange.liquid.LiquidExchange
import zio.logging._

object WatchOrderStreamUC {
  def watch = for {
    stream <- LiquidExchange.ordersStream
    _      <- stream.foreach(o => log.info(o.toString))
  } yield ()
}
