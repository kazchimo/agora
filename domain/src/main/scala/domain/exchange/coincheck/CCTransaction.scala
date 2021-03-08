package domain.exchange.coincheck

import domain.currency.Currency
import domain.lib.{EnumZio, VOFactory}
import enumeratum._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import CCTransaction._

final case class CCTransaction(
  id: CCTraId,
  sold: Currency,
  bought: Currency,
  side: CCTraSide,
  createdAt: CCTraCreatedAt,
  rate: CCTraRate
)

object CCTransaction {
  @newtype case class CCTraId(value: Long Refined Positive)
  object CCTraId extends VOFactory

  @newtype case class CCTraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object CCTraCreatedAt extends VOFactory

  @newtype case class CCTraRate(value: Double Refined Positive)
  object CCTraRate extends VOFactory

  sealed abstract class CCTraSide(override val entryName: String)
      extends Serializable with Product with EnumEntry

  object CCTraSide extends Enum[CCTraSide] with EnumZio[CCTraSide] {
    val values: IndexedSeq[CCTraSide] = findValues

    case object Buy  extends CCTraSide("buy")
    case object Sell extends CCTraSide("sell")
  }
}
