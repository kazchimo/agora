package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import zio.{RIO, stream}

private[liquid] trait OrdersStream extends WebSocketHandler {
  self: LiquidExchange.Service =>
  override def ordersStream
    : RIO[AllEnv, stream.Stream[Throwable, LiquidOrder]] = ???
}
