package domain.exchange

import zio.{Has, IO, ZIO}

package object coincheck {
  type CoincheckExchange = Has[CoincheckExchange.Service]

  object CoincheckExchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
    }

    def transactions: ZIO[CoincheckExchange, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get.transactions)
  }

}
