package exchange

import eu.timepit.refined.types.string.NonEmptyString
import exchange.ExchangeConfig.{CCApiKey, CCSecretKey}
import io.estatico.newtype.macros.newtype
import zio.ZIO

trait IExchange[T <: IExchange[_]] {
  type Conf = ExchangeConfig[T]

  def transactions: ZIO[Conf, String, String]
}

trait ExchangeConfig[T <: IExchange[_]] {
  val apiKey: CCApiKey
  val secretKey: CCSecretKey
}

object ExchangeConfig {
  @newtype final case class CCApiKey(value: NonEmptyString)
  @newtype final case class CCSecretKey(value: NonEmptyString)
}
