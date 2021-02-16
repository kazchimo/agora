package domain.exchange.coincheck

import domain.currency.Currency
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.factory.{SumVOFactory, VOFactory}

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

  sealed trait CCTraSide extends Serializable with Product {
    val v: String
  }

  object CCTraSide extends SumVOFactory {
    override type VO = CCTraSide
    override val sums: Seq[CCTraSide]        = Seq(Buy, Sell)
    override def extractValue(v: VO): String = v.v
  }

  case object Buy extends CCTraSide {
    override val v: String = "buy"
  }

  case object Sell extends CCTraSide {
    override val v: String = "sell"
  }
}
