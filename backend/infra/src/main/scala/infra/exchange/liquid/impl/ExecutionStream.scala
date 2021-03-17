package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidExecution}
import zio.{ZIO, stream}

private[liquid] trait ExecutionStream { self: LiquidExchange.Service =>
  override def executionStream
    : ZIO[AllEnv, Throwable, stream.Stream[Throwable, LiquidExecution]] = ???
}
