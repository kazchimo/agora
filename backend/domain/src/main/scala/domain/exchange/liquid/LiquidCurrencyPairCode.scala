package domain.exchange.liquid

import domain.lib.ZEnum
import enumeratum._

sealed abstract class LiquidCurrencyPairCode(override val entryName: String)
    extends EnumEntry

object LiquidCurrencyPairCode extends ZEnum[LiquidCurrencyPairCode] {
  val values: IndexedSeq[LiquidCurrencyPairCode] = findValues

  case object BtcJpy extends LiquidCurrencyPairCode("btcjpy")
}
