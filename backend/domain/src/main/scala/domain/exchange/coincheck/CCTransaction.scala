package domain.exchange.coincheck

import domain.currency.Currency
import domain.exchange.coincheck.CCTransaction._
import domain.lib.{ZEnum, VOFactory}
import enumeratum._
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.refined.{PositiveDouble, PositiveLong}

final case class CCTransaction(
  id: CCTraId,
  sold: Currency,
  bought: Currency,
  side: CCTraSide,
  createdAt: CCTraCreatedAt,
  rate: CCTraRate
)

object CCTransaction {
  @newtype case class CCTraId(value: PositiveLong)
  object CCTraId extends VOFactory

  @newtype case class CCTraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object CCTraCreatedAt extends VOFactory

  @newtype case class CCTraRate(value: PositiveDouble)
  object CCTraRate extends VOFactory

  sealed abstract class CCTraSide(override val entryName: String)
      extends Serializable with Product with EnumEntry

  object CCTraSide extends ZEnum[CCTraSide] {
    val values: IndexedSeq[CCTraSide] = findValues

    case object Buy  extends CCTraSide("buy")
    case object Sell extends CCTraSide("sell")
  }
}
