package domain.exchange

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import lib.error.ClientDomainError
import sttp.client3.asynchttpclient.zio.SttpClient
import zio._
import zio.logging.Logging
import zio.macros.accessible
import zio.stream.{Stream, UStream}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  @accessible
  object CoincheckExchange {
    trait Service {
      def transactions: RIO[SttpClient with Conf, Seq[CCTransaction]]
      def orders(
        order: CCOrderRequest
      ): RIO[SttpClient with ZEnv with Conf, CCOrder]
      // Get unsettled orders
      def openOrders: RIO[SttpClient with Conf, Seq[CCOrder]]
      def cancelOrder(
        id: CCOrderId
      ): RIO[SttpClient with Logging with Conf, CCOrderId]
      def cancelStatus(id: CCOrderId): RIO[SttpClient with Conf, Boolean]
      def publicTransactions
        : ZIO[SttpClient with ZEnv with Logging with Conf, Throwable, UStream[
          CCPublicTransaction
        ]]
      def balance: RIO[SttpClient with Conf with Logging, CCBalance]
    }

    val notStubbed: ZIO[Any, Throwable, Nothing] = ZIO.fail(
      ClientDomainError("Access Stub object before method is property stubbed")
    )

    def stubLayer(
      transactionsRes: RIO[SttpClient, Seq[CCTransaction]] = notStubbed,
      ordersRes: RIO[SttpClient with zio.ZEnv, CCOrder] = notStubbed,
      openOrdersRes: RIO[SttpClient, Seq[CCOrder]] = notStubbed,
      cancelOrderRes: RIO[SttpClient with Logging, CCOrderId] = notStubbed,
      cancelStatusRes: RIO[SttpClient, Boolean] = notStubbed,
      publicTransactionsRes: ZIO[
        SttpClient with zio.ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = notStubbed,
      balanceRes: RIO[SttpClient with Conf with Logging, CCBalance]
    ): ULayer[CoincheckExchange] = ZLayer.succeed(new Service {
      override def transactions: RIO[SttpClient, Seq[CCTransaction]] =
        transactionsRes

      override def orders(
        order: CCOrderRequest
      ): RIO[SttpClient with zio.ZEnv, CCOrder] = ordersRes

      override def openOrders: RIO[SttpClient, Seq[CCOrder]] = openOrdersRes

      override def cancelOrder(
        id: CCOrderId
      ): RIO[SttpClient with Logging, CCOrderId] = cancelOrderRes

      override def cancelStatus(id: CCOrderId): RIO[SttpClient, Boolean] =
        cancelStatusRes

      override def publicTransactions: ZIO[
        SttpClient with zio.ZEnv with Logging,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = publicTransactionsRes

      override def balance: RIO[SttpClient with Conf with Logging, CCBalance] =
        balanceRes
    })
  }
}
