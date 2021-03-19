package usecase.liquid

import domain.exchange.liquid.LiquidExchange
import domain.exchange.liquid.LiquidExchange.OrderSide
import zio.logging._

object WatchOrderStreamUC {
  def watch = for {
    stream <- LiquidExchange.ordersStream(OrderSide.Buy)
    _      <- stream.foreach(o => log.info(o.toString))
  } yield ()
}
