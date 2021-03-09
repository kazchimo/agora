package domain.exchange

import domain.exchange.coincheck.CCOrder.{CCOrderId, CCOrderType}
import zio._
import zio.macros.accessible
import zio.stream.UStream

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  @accessible
  object CoincheckExchange {
    import domain.AllEnv
    trait Service {
      def transactions: RIO[AllEnv, Seq[CCTransaction]]
      def orders(order: CCOrderRequest[_ <: CCOrderType]): RIO[AllEnv, CCOrder]
      // Get unsettled orders
      def openOrders: RIO[AllEnv, Seq[CCOpenOrder]]
      def cancelOrder(id: CCOrderId): RIO[AllEnv, CCOrderId]
      def cancelStatus(id: CCOrderId): RIO[AllEnv, Boolean]
      def publicTransactions
        : ZIO[AllEnv, Throwable, UStream[CCPublicTransaction]]
      def balance: RIO[AllEnv, CCBalance]
    }
  }
}
