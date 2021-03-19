package domain.exchange

import domain.exchange.liquid.LiquidOrder.{OrderType, Side}
import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, RIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def createOrder[O <: OrderType, S <: Side](
        orderRequest: LiquidOrderRequest[O, S]
      ): RIO[AllEnv, Unit]
      def ordersStream(
        side: Side
      ): RIO[AllEnv, Stream[Throwable, Seq[LiquidOrder]]]
      def productsStream: RIO[AllEnv, Stream[Throwable, LiquidProduct]]
      def executionStream: RIO[AllEnv, Stream[Throwable, LiquidExecution]]
    }
  }
}
