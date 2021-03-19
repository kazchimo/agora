package domain.currency

import domain.lib.EnumZio
import enumeratum._

sealed abstract class TickerSymbol(override val entryName: String)
    extends Serializable with Product with EnumEntry

object TickerSymbol extends EnumZio[TickerSymbol] {
  val values: IndexedSeq[TickerSymbol] = findValues

  case object BitCoin extends TickerSymbol("btc")
  case object Jpy     extends TickerSymbol("jpy")
}
