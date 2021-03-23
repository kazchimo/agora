package domain.exchange

import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Side}
import zio.macros.accessible
import zio.stream.Stream
import zio.{Has, RIO}

package object liquid {
  type LiquidExchange = Has[LiquidExchange.Service]

  @accessible
  object LiquidExchange extends Liquid {
    import domain.AllEnv

    trait Service {
      def getOrder(id: Id): RIO[AllEnv, LiquidOrder]
      def cancelOrder(id: Id): RIO[AllEnv, Unit]
      def createOrder[O <: OrderType, S <: Side](
        orderRequest: LiquidOrderRequest[O, S]
      ): RIO[AllEnv, LiquidOrder]
      def orderBookStream(
        side: Side
      ): RIO[AllEnv, Stream[Throwable, Seq[OrderOnBook]]]
      def productsStream: RIO[AllEnv, Stream[Throwable, LiquidProduct]]
      def ordersStream: RIO[AllEnv, Stream[Throwable, LiquidOrder]]
      def executionStream: RIO[AllEnv, Stream[Throwable, LiquidExecution]]
    }
  }
}
