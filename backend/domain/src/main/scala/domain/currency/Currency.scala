package domain.currency

import domain.currency.Currency.CurQuantity
import io.estatico.newtype.macros.newtype

final case class Currency(tickerSymbol: TickerSymbol, quantity: CurQuantity)

object Currency {
  @newtype case class CurQuantity(value: Double)

  def apply(tickerSymbol: TickerSymbol, quantity: Double): Currency =
    Currency(tickerSymbol, CurQuantity(quantity))
}
