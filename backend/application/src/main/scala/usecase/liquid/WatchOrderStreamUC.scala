package usecase.liquid

import domain.exchange.liquid.LiquidExchange
import domain.exchange.liquid.LiquidExchange.OrderSide
import zio.logging._

object WatchOrderStreamUC {
  def watch = for {
    buyStream  <- LiquidExchange.ordersStream(OrderSide.Buy)
    sellStream <- LiquidExchange.ordersStream(OrderSide.Sell)
    buyFiber   <- buyStream.foreach(o => log.info(o.toString)).fork
    sellFiber  <- sellStream.foreach(o => log.info(o.toString)).fork
    _          <- buyFiber.join
    _          <- sellFiber.join
  } yield ()
}
