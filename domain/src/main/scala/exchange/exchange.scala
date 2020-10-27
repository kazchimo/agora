package exchange

import zio.macros.accessible
import zio.{Has, IO, ZIO}

trait IExchange[T <: IExchange[_]] {
  type Conf <: ExchangeConfig[T]

  def transactions: ZIO[Has[Conf], String, String]
}

trait ExchangeConfig[T <: IExchange[_]]


package object exchange {
  type Exchange = Has[Exchange.Service]

  @accessible
  object Exchange {
    trait Service {
      def transactions: IO[String, String]
    }
  }
}

