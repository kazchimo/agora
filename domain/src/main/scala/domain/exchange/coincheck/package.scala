package domain.exchange

import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CCOrder.{CCOrderId, CCOrderType}
import lib.error.ClientDomainError
import sttp.client3.asynchttpclient.zio.SttpClient
import zio._
import zio.logging.Logging
import zio.macros.accessible
import zio.stream.{Stream, UStream}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]
  type Env               = SttpClient with ZEnv with Logging with Conf with Nonce

  @accessible
  object CoincheckExchange {
    trait Service {
      def transactions: RIO[Env, Seq[CCTransaction]]
      def orders(order: CCOrderRequest[_ <: CCOrderType]): RIO[Env, CCOrder]
      // Get unsettled orders
      def openOrders: RIO[Env, Seq[CCOrder]]
      def cancelOrder(id: CCOrderId): RIO[Env, CCOrderId]
      def cancelStatus(id: CCOrderId): RIO[Env, Boolean]
      def publicTransactions: ZIO[Env, Throwable, UStream[CCPublicTransaction]]
      def balance: RIO[Env, CCBalance]
    }

    val notStubbed: ZIO[Any, Throwable, Nothing] = ZIO.fail(
      ClientDomainError("Access Stub object before method is property stubbed")
    )

    def stubLayer(
      transactionsRes: RIO[Env, Seq[CCTransaction]] = notStubbed,
      ordersRes: RIO[Env, CCOrder] = notStubbed,
      openOrdersRes: RIO[Env, Seq[CCOrder]] = notStubbed,
      cancelOrderRes: RIO[Env, CCOrderId] = notStubbed,
      cancelStatusRes: RIO[Env, Boolean] = notStubbed,
      publicTransactionsRes: ZIO[
        Env,
        Throwable,
        Stream[Nothing, CCPublicTransaction]
      ] = notStubbed,
      balanceRes: RIO[Env, CCBalance] = notStubbed
    ): ULayer[CoincheckExchange] = ZLayer.succeed(new Service {
      override def transactions: RIO[Env, Seq[CCTransaction]] = transactionsRes

      override def orders(
        order: CCOrderRequest[_ <: CCOrderType]
      ): RIO[Env, CCOrder] = ordersRes

      override def openOrders: RIO[Env, Seq[CCOrder]] = openOrdersRes

      override def cancelOrder(id: CCOrderId): RIO[Env, CCOrderId] =
        cancelOrderRes

      override def cancelStatus(id: CCOrderId): RIO[Env, Boolean] =
        cancelStatusRes

      override def publicTransactions
        : ZIO[Env, Throwable, Stream[Nothing, CCPublicTransaction]] =
        publicTransactionsRes

      override def balance: RIO[Env, CCBalance] = balanceRes
    })
  }
}
