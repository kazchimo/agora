package infra.exchange.coincheck

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import infra.exchange.coincheck.CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import io.estatico.newtype.macros.newtype
import lib.refined._
import zio.IO

final case class CoinCheckExchangeConfig(
  apiKey: CCEApiKey,
  secretKey: CCESecretKey
)

object CoinCheckExchangeConfig {
  @newtype final case class CCEApiKey(value: NonEmptyString)
  object CCEApiKey {
    def apply(value: String): IO[String, CCEApiKey] =
      refineVZ[NonEmpty, String](value).map(CCEApiKey(_))
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey {
    def apply(value: String): IO[String, CCESecretKey] =
      refineVZ[NonEmpty, String](value).map(CCESecretKey(_))
  }
}
