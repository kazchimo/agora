package domain.exchange

import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Side}
import lib.zio.EStream
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
      def getTrades(
        productId: Option[LiquidProduct.Id] = None,
        fundingCurrency: Option[FundingCurrency] = None,
        status: Option[Trade.Status] = None,
        side: Option[Trade.Side] = None,
        tradingType: Option[Trade.TradingType] = None
      ): RIO[AllEnv, Seq[Trade]]

      // websocket apis
      def orderBookStream(side: Side): RIO[AllEnv, EStream[Seq[OrderOnBook]]]
      def productsStream: RIO[AllEnv, EStream[LiquidProduct]]
      def ordersStream: RIO[AllEnv, EStream[LiquidOrder]]
      def executionStream: RIO[AllEnv, EStream[LiquidExecution]]
      def tradesStream: RIO[AllEnv, EStream[Trade]]
    }
  }
}
