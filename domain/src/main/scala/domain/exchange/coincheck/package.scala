package domain.exchange

import sttp.client3.asynchttpclient.zio.SttpClient
import zio._
import zio.logging.Logging
import zio.macros.accessible
import zio.stream.Stream

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  @accessible
  object CoincheckExchange {
    trait Service {
      def transactions: RIO[SttpClient, Seq[CCTransaction]]
      def orders(order: CCOrderRequest): RIO[SttpClient with ZEnv, CCOrder]
      // Get unsettled orders
      def openOrders: RIO[SttpClient, Seq[CCOrder]]
      def publicTransactions: ZIO[
        SttpClient with ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ]
    }
  }
}
