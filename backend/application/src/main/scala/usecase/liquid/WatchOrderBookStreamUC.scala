package usecase.liquid

import domain.exchange.liquid.LiquidExchange
import domain.exchange.liquid.LiquidOrder.Side
import zio.logging._

object WatchOrderBookStreamUC {
  def watch = for {
    buyStream  <- LiquidExchange.orderBookStream(Side.Buy)
    sellStream <- LiquidExchange.orderBookStream(Side.Sell)
    buyFiber   <- buyStream.foreach(o => log.info(o.toString)).fork
    sellFiber  <- sellStream.foreach(o => log.info(o.toString)).fork
    _          <- buyFiber.join
    _          <- sellFiber.join
  } yield ()
}
