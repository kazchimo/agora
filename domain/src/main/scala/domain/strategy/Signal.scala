package domain.strategy

import enumeratum._

sealed trait Signal extends EnumEntry

object Signal extends Enum[Signal] {
  val values: IndexedSeq[Signal] = findValues

  case object Buy  extends Signal
  case object Sell extends Signal
}
