package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidProduct}
import zio.ZIO
import zio.stream.UStream

final case class LiquidExchangeImpl() extends LiquidExchange.Service {
  override def productsStream: ZIO[AllEnv, Throwable, UStream[LiquidProduct]] =
    ???
}
