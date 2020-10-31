package domain.exchange

import zio.{Has, IO, ZIO}

package object coincheck {
  type Exchange = Has[Exchange.Service]

  object Exchange {
    trait Service {
      def transactions: IO[Throwable, Seq[CCTransaction]]
    }

    def transactions: ZIO[Exchange, Throwable, Seq[CCTransaction]] =
      ZIO.accessM(_.get.transactions)
  }

}
