package exchange

import zio.{Has, ZIO}

trait IExchange[T <: IExchange[_]] {
  type Conf <: ExchangeConfig[T]

  def transactions: ZIO[Has[Conf], String, String]
}

trait ExchangeConfig[T <: IExchange[_]]

