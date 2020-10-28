import zio.{Has, IO, ZIO}

package object exchange {
  type Exchange = Has[Exchange.Service]

  object Exchange {
    trait Service {
      def transactions: IO[Throwable, Seq[Transaction]]
    }

    def transactions: ZIO[Exchange, Throwable, Seq[Transaction]] =
      ZIO.accessM(_.get.transactions)
  }

}
