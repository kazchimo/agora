package domain.exchange

import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{Has, IO, RIO, ZIO}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  object CoincheckExchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
      def orders(order: CCOrder): RIO[SttpClient, Unit]
    }

    def transactions: ZIO[CoincheckExchange, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get.transactions)

    def orders(
      order: CCOrder
    ): ZIO[SttpClient with CoincheckExchange, Throwable, Unit] =
      ZIO.accessM(_.get[CoincheckExchange.Service].orders(order))
  }

}
