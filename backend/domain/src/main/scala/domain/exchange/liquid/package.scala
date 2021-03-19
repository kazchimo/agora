package domain.exchange

import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Side}
import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, RIO, ZIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange {
    import domain.AllEnv

    trait Service {
      def createOrder[O <: OrderType, S <: Side](
        orderRequest: LiquidOrderRequest[O, S]
      ): RIO[AllEnv, Id]
      def ordersStream(
        side: Side
      ): RIO[AllEnv, Stream[Throwable, Seq[OrderOnBook]]]
      def productsStream: RIO[AllEnv, Stream[Throwable, LiquidProduct]]
      def executionStream: RIO[AllEnv, Stream[Throwable, LiquidExecution]]
    }
  }
}
