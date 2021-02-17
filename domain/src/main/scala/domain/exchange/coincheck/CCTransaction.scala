package domain.exchange.coincheck

import domain.currency.Currency
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory
import CCTransaction._
import enumeratum._

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
  object CCTraId extends VOFactory[Long, Positive] {
    override type VO = CCTraId
  }

  @newtype case class CCTraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object CCTraCreatedAt extends VOFactory[String, NonEmpty] {
    override type VO = CCTraCreatedAt
  }

  @newtype case class CCTraRate(value: Double Refined Positive)
  object CCTraRate extends VOFactory[Double, Positive] {
    override type VO = CCTraRate
  }

  sealed abstract class CCTraSide(override val entryName: String)
      extends Serializable with Product with EnumEntry

  object CCTraSide extends Enum[CCTraSide]  {
    val values: IndexedSeq[CCTraSide] = findValues
    
    case object Buy extends CCTraSide("buy")
    case object Sell extends CCTraSide("sell")
  }
}
