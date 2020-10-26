package currency

import currency.Currency.{CurQuantity, CurTickerSymbol}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import eu.timepit.refined.auto._

sealed trait Currency {
  val quantity: CurQuantity
  val tickerSymbol: CurTickerSymbol
}

object Currency {
  @newtype case class CurQuantity(value: Double Refined Positive)
  @newtype case class CurTickerSymbol(value: NonEmptyString)
}

final case class BitCoin(quantity: CurQuantity) extends Currency {
  override val tickerSymbol: CurTickerSymbol = CurTickerSymbol("btc")
}

final case class Yen(quantity: CurQuantity) extends Currency {
  override val tickerSymbol: CurTickerSymbol = CurTickerSymbol("jpy")
}
