package domain.exchange.liquid

import domain.lib.EnumZio
import enumeratum._

sealed abstract class LiquidCurrencyPairCode(override val entryName: String)
    extends EnumEntry

object LiquidCurrencyPairCode
    extends Enum[LiquidCurrencyPairCode] with EnumZio[LiquidCurrencyPairCode] {
  val values: IndexedSeq[LiquidCurrencyPairCode] = findValues

  case object BtcJpy extends LiquidCurrencyPairCode("btcjpy")
}