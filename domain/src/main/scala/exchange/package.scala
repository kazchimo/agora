import zio.{Has, IO, ZIO}

package object exchange {
  type Exchange = Has[Exchange.Service]

  object Exchange {
    trait Service {
      def transactions: IO[String, String]
    }

    def transactions: ZIO[Exchange, String, String] =
      ZIO.accessM(_.get.transactions)
  }

}
