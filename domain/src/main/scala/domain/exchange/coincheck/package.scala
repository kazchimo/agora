package domain.exchange

import sttp.client3.asynchttpclient.zio.SttpClient
import zio._
import zio.stream.Stream

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]
  type CoincheckEnv      = SttpClient with CoincheckExchange with ZEnv

  object CoincheckExchange {
    trait Service {
      def transactions: RIO[SttpClient, Seq[CCTransaction]]
      def orders(order: CCOrder): RIO[SttpClient with ZEnv, Unit]
      def publicTransactions
        : ZIO[SttpClient with ZEnv, Throwable, Stream[Nothing, String]]
    }

    def transactions: ZIO[CoincheckEnv, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get[CoincheckExchange.Service].transactions)

    def orders(order: CCOrder): ZIO[CoincheckEnv, Throwable, Unit] =
      ZIO.accessM(_.get[CoincheckExchange.Service].orders(order))

    def publicTransactions
      : ZIO[CoincheckEnv, Throwable, Stream[Nothing, String]] =
      ZIO.accessM(_.get[CoincheckExchange.Service].publicTransactions)
  }
}
