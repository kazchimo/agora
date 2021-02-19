package domain.exchange

import domain.exchange.coincheck.CCOrder.CCOrderId
import lib.error.ClientDomainError
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
      def cancelOrder(id: CCOrderId): RIO[SttpClient, CCOrderId]
      def cancelStatus(id: CCOrderId): RIO[SttpClient, Boolean]
      def publicTransactions: ZIO[
        SttpClient with ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ]
    }

    val notStubbed: ZIO[Any, Throwable, Nothing] = ZIO.fail(
      ClientDomainError("Access Stub object before method is property stubbed")
    )

    def stubLayer(
      transactionsRes: RIO[SttpClient, Seq[CCTransaction]] = notStubbed,
      ordersRes: RIO[SttpClient with zio.ZEnv, CCOrder] = notStubbed,
      openOrdersRes: RIO[SttpClient, Seq[CCOrder]] = notStubbed,
      cancelOrderRes: RIO[SttpClient, CCOrderId] = notStubbed,
      cancelStatusRes: RIO[SttpClient, Boolean] = notStubbed,
      publicTransactionsRes: ZIO[
        SttpClient with zio.ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = notStubbed
    ): ULayer[CoincheckExchange] = ZLayer.succeed(new Service {
      override def transactions: RIO[SttpClient, Seq[CCTransaction]] =
        transactionsRes

      override def orders(
        order: CCOrderRequest
      ): RIO[SttpClient with zio.ZEnv, CCOrder] = ordersRes

      override def openOrders: RIO[SttpClient, Seq[CCOrder]] = openOrdersRes

      override def cancelOrder(id: CCOrderId): RIO[SttpClient, CCOrderId] =
        cancelOrderRes

      override def cancelStatus(id: CCOrderId): RIO[SttpClient, Boolean] =
        cancelStatusRes

      override def publicTransactions: ZIO[
        SttpClient with zio.ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = publicTransactionsRes
    })
  }
}
