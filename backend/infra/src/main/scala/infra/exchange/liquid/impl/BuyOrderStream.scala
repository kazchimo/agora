package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import zio.{RIO, stream}

trait BuyOrderStream { self: LiquidExchange.Service =>
  override def buyOrderStream
    : RIO[AllEnv, stream.Stream[Throwable, LiquidOrder]] = ???
}
