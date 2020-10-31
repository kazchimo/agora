package domain.exchange

import zio.{Has, IO, Task, ZIO}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  object CoincheckExchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
      def orders: Task[Unit]
    }

    def transactions: ZIO[CoincheckExchange, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get.transactions)

    def orders: ZIO[CoincheckExchange, Throwable, Unit] =
      ZIO.accessM(_.get.orders)
  }

}
