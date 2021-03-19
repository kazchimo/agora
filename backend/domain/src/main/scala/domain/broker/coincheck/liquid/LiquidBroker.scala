package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.Id
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import zio.ZIO
import zio.duration._

object LiquidBroker {
  def waitFilled(id: Id): ZIO[AllEnv, Throwable, LiquidOrder] = LiquidExchange
    .getOrder(id).repeatUntilM(o =>
      if (o.filled) ZIO.succeed(true) else ZIO.sleep(1.seconds).as(false)
    )
}
