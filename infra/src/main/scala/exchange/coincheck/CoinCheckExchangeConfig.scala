package exchange.coincheck

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import exchange.coincheck.CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import io.estatico.newtype.macros.newtype
import zio.{IO, ZIO}

final case class CoinCheckExchangeConfig(
  apiKey: CCEApiKey,
  secretKey: CCESecretKey
)

object CoinCheckExchangeConfig {
  @newtype final case class CCEApiKey(value: NonEmptyString)
  object CCEApiKey {
    def apply(value: String): IO[String, CCEApiKey] =
      ZIO.fromEither(refineV[NonEmpty](value)).map(CCEApiKey(_))
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey {
    def apply(value: String): IO[String, CCESecretKey] =
      ZIO.fromEither(refineV[NonEmpty](value)).map(CCESecretKey(_))
  }
}
