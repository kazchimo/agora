package usecase.liquid

import domain.exchange.liquid.LiquidExchange
import zio.logging.log

object WatchExecutionStreamUC {
  def watch = for {
    stream <- LiquidExchange.executionStream
    _      <- stream.foreach(e => log.info(e.toString))
  } yield ()
}
