package usecase.liquid

import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import zio.stream._
import zio.logging._

object WatchOrderStreamUC {
  def watch = for {
    stream: Stream[Throwable, LiquidOrder] <- LiquidExchange.ordersStream
    _                                      <- stream.foreach(o => log.info(o.toString))
  } yield ()
}
