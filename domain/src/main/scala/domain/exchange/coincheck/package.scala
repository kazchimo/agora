package domain.exchange

import sttp.client3.asynchttpclient.zio.SttpClient
import zio.stream.{Stream, ZStream}
import zio.{Has, IO, RIO, ZIO}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]
  type CoincheckEnv      = SttpClient with CoincheckExchange

  object CoincheckExchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
      def orders(order: CCOrder): RIO[SttpClient, Unit]
      def publicTransactions
        : ZIO[SttpClient, Throwable, ZStream[Any, Nothing, String]]
    }

    def transactions: ZIO[CoincheckEnv, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get[CoincheckExchange.Service].transactions)

    def orders(order: CCOrder): ZIO[CoincheckEnv, Throwable, Unit] =
      ZIO.accessM(_.get[CoincheckExchange.Service].orders(order))
  }

  def publicTransactions
    : ZIO[CoincheckEnv, Throwable, Stream[Nothing, String]] =
    ZIO.accessM(_.get[CoincheckExchange.Service].publicTransactions)

}
