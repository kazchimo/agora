package domain.exchange

import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Side}
import lib.zio.EStream
import zio.macros.accessible
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
      def getTrades(params: GetTradesParams): RIO[AllEnv, Seq[Trade]]
      def closeTrade(id: Trade.Id): RIO[AllEnv, Unit]

      // websocket apis
      def orderBookStream(side: Side): RIO[AllEnv, EStream[Seq[OrderOnBook]]]
      def productsStream: RIO[AllEnv, EStream[LiquidProduct]]
      def ordersStream: RIO[AllEnv, EStream[LiquidOrder]]
      def executionStream: RIO[AllEnv, EStream[LiquidExecution]]
      def tradesStream: RIO[AllEnv, EStream[Trade]]
    }
  }
}
