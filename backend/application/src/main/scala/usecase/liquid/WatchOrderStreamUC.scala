package usecase.liquid

import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import zio.logging._
import zio.stream._

object WatchOrderStreamUC {
  def watch = for {
    stream: Stream[Throwable, LiquidOrder] <- LiquidExchange.ordersStream
    _                                      <- stream.foreach(o => log.info(o.toString))
  } yield ()
}
