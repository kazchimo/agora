package domain.exchange

import zio.{Has, IO, Task, ZIO}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  object CoincheckExchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
      def orders(order: CCOrder): Task[Unit]
    }

    def transactions: ZIO[CoincheckExchange, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get.transactions)

    def orders(order: CCOrder): ZIO[CoincheckExchange, Throwable, Unit] =
      ZIO.accessM(_.get.orders(order))
  }

}
