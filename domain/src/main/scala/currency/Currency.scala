package domain.currency

import domain.currency.Currency.{CurrencyQuantity, CurrencyTickerSymbol}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.auto._

sealed trait Currency {
  val quantity: CurrencyQuantity
  val tickerSymbol: CurrencyTickerSymbol
}

object Currency {
  @newtype case class CurrencyQuantity(value: Double Refined Positive)
  @newtype case class CurrencyTickerSymbol(value: NonEmptyString)
}

final case class BitCoin(quantity: CurrencyQuantity) extends Currency {
  override final val tickerSymbol: CurrencyTickerSymbol = CurrencyTickerSymbol(
    "JPY"
  )
}

final case class Yen(
  quantity: CurrencyQuantity,
  tickerSymbol: CurrencyTickerSymbol
) extends Currency
