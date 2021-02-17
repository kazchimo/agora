package domain.exchange.coincheck

import domain.exchange.coincheck.CCPublicTransaction._
import domain.lib.VOFactory
import enumeratum._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.all.NonEmptyString
import io.estatico.newtype.macros.newtype

final case class CCPublicTransaction(
  id: CCPubTraId,
  pair: CCPubTraPair,
  rate: CCPubTraRate,
  quantity: CCPubTraQuantity,
  side: CCPubTraSide
)

object CCPublicTransaction {
  @newtype final case class CCPubTraId(value: Refined[Long, Positive])
  object CCPubTraId extends VOFactory[Long, Positive] {
    override type VO = CCPubTraId
  }

  @newtype final case class CCPubTraPair(value: NonEmptyString)
  object CCPubTraPair extends VOFactory[String, NonEmpty] {
    override type VO = CCPubTraPair
  }

  @newtype final case class CCPubTraRate(value: Refined[Double, Positive])
  object CCPubTraRate extends VOFactory[Double, Positive] {
    override type VO = CCPubTraRate
  }

  @newtype final case class CCPubTraQuantity(value: Refined[Double, Positive])
  object CCPubTraQuantity extends VOFactory[Double, Positive] {
    override type VO = CCPubTraQuantity
  }

  sealed abstract class CCPubTraSide(override val entryName: String)
      extends EnumEntry
  object CCPubTraSide extends Enum[CCPubTraSide] {
    val values: IndexedSeq[CCPubTraSide] = findValues

    case object CCPubTraBuy  extends CCPubTraSide("buy")
    case object CCPubTraSell extends CCPubTraSide("sell")
  }
}
