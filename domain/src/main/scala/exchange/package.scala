import zio.{Has, IO, ZIO}

package object exchange {
  type Exchange = Has[Exchange.Service]

  object Exchange {
    trait Service {
      def transactions: IO[Throwable, String]
    }

    def transactions: ZIO[Exchange, Throwable, String] =
      ZIO.accessM(_.get.transactions)
  }

}
