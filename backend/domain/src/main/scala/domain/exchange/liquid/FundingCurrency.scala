package domain.exchange.liquid

import domain.lib.ZEnum
import enumeratum.EnumEntry.Lowercase

sealed trait FundingCurrency extends Lowercase

object FundingCurrency extends ZEnum[FundingCurrency] {
  override def values: IndexedSeq[FundingCurrency] = findValues

  case object Jpy extends FundingCurrency
  case object Usd extends FundingCurrency
}
