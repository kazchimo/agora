package domain.exchange.coincheck

import domain.exchange.coincheck.CCPublicTransaction._
import domain.lib.VOFactory
import enumeratum._
import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.refined.{NonNegativeDouble, PositiveDouble, PositiveLong}

final case class CCPublicTransaction(
  id: CCPubTraId,
  pair: CCPubTraPair,
  rate: CCPubTraRate,
  quantity: CCPubTraQuantity,
  side: CCPubTraSide
)

object CCPublicTransaction {
  @newtype final case class CCPubTraId(value: PositiveLong)
  object CCPubTraId extends VOFactory

  @newtype final case class CCPubTraPair(value: NonEmptyString)
  object CCPubTraPair extends VOFactory

  @newtype final case class CCPubTraRate(value: PositiveDouble)
  object CCPubTraRate extends VOFactory

  @newtype final case class CCPubTraQuantity(value: NonNegativeDouble)
  object CCPubTraQuantity extends VOFactory

  sealed abstract class CCPubTraSide(override val entryName: String)
      extends EnumEntry
  object CCPubTraSide extends Enum[CCPubTraSide] {
    val values: IndexedSeq[CCPubTraSide] = findValues

    case object CCPubTraBuy  extends CCPubTraSide("buy")
    case object CCPubTraSell extends CCPubTraSide("sell")
  }
}
