package domain.exchange

import domain.exchange.coincheck.CCOrder.{CCOrderId, CCOrderType}
import lib.error.ClientDomainError
import zio._
import zio.macros.accessible
import zio.stream.{Stream, UStream}

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

    val notStubbed: ZIO[Any, Throwable, Nothing] = ZIO.fail(
      ClientDomainError("Access Stub object before method is property stubbed")
    )

    def stubLayer(
      transactionsRes: RIO[AllEnv, Seq[CCTransaction]] = notStubbed,
      ordersRes: RIO[AllEnv, CCOrder] = notStubbed,
      openOrdersRes: RIO[AllEnv, Seq[CCOpenOrder]] = notStubbed,
      cancelOrderRes: RIO[AllEnv, CCOrderId] = notStubbed,
      cancelStatusRes: RIO[AllEnv, Boolean] = notStubbed,
      publicTransactionsRes: ZIO[
        AllEnv,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = notStubbed,
      balanceRes: RIO[AllEnv, CCBalance] = notStubbed
    ): ULayer[CoincheckExchange] = ZLayer.succeed(new Service {
      override def transactions: RIO[AllEnv, Seq[CCTransaction]] =
        transactionsRes

      override def orders(
        order: CCOrderRequest[_ <: CCOrderType]
      ): RIO[AllEnv, CCOrder] = ordersRes

      override def openOrders: RIO[AllEnv, Seq[CCOpenOrder]] = openOrdersRes

      override def cancelOrder(id: CCOrderId): RIO[AllEnv, CCOrderId] =
        cancelOrderRes

      override def cancelStatus(id: CCOrderId): RIO[AllEnv, Boolean] =
        cancelStatusRes

      override def publicTransactions
        : ZIO[AllEnv, Throwable, Stream[Nothing, CCPublicTransaction]] =
        publicTransactionsRes

      override def balance: RIO[AllEnv, CCBalance] = balanceRes
    })
  }
}
