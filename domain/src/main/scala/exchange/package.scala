import zio.{Has, IO}
import zio.macros.accessible

package object exchange {
  type Exchange = Has[Exchange.Service]

  @accessible
  object Exchange {
    trait Service {
      def transactions: IO[String, String]
    }
  }
}
