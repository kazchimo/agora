package exchange

import eu.timepit.refined.types.string.NonEmptyString
import exchange.ExchangeConfig.{ExcApiKey, ExcSecretKey}
import io.estatico.newtype.macros.newtype
import zio.ZIO

trait IExchange[T <: IExchange[_]] {
  type Conf = ExchangeConfig[T]

  def transactions: ZIO[Conf, String, String]
}

trait ExchangeConfig[T <: IExchange[_]] {
  val apiKey: ExcApiKey
  val secretKey: ExcSecretKey
}

object ExchangeConfig {
  @newtype final case class ExcApiKey(value: NonEmptyString)
  @newtype final case class ExcSecretKey(value: NonEmptyString)
}
