package infra.exchange.coincheck

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import infra.exchange.coincheck.CoinCheckExchangeConfig.{
  CCEApiKey,
  CCESecretKey
}
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory

final case class CoinCheckExchangeConfig(
  apiKey: CCEApiKey,
  secretKey: CCESecretKey
)

object CoinCheckExchangeConfig {
  @newtype final case class CCEApiKey(value: NonEmptyString)
  object CCEApiKey extends VOFactory[String, NonEmpty] {
    override type VO = CCEApiKey
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey extends VOFactory[String, NonEmpty] {
    override type VO = CCESecretKey
  }
}
