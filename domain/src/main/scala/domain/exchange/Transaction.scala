package domain.exchange

import domain.DomainError
import domain.currency.Currency
import domain.exchange.Transaction.{TraCreatedAt, TraId, TraRate, TraSide}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory
import zio.{IO, ZIO}

final case class Transaction(
  id: TraId,
  sold: Currency,
  bought: Currency,
  side: TraSide,
  createdAt: TraCreatedAt,
  rate: TraRate
)

object Transaction {
  @newtype case class TraId(value: Long Refined Positive)
  object TraId extends VOFactory[Long, Positive] { override type VO = TraId }

  @newtype case class TraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object TraCreatedAt extends VOFactory[String, NonEmpty] {
    override type VO = TraCreatedAt
  }

  @newtype case class TraRate(value: Double Refined Positive)
  object TraRate extends VOFactory[Double, Positive] {
    override type VO = TraRate
  }

  sealed trait TraSide {
    val v: String
    val isBuy: Boolean
    val isSell: Boolean
  }

  object TraSide {
    def apply(v: String): IO[DomainError, TraSide] = v match {
      case Buy.v  => ZIO.succeed(Buy)
      case Sell.v => ZIO.succeed(Sell)
      case _      =>
        ZIO.fail(
          DomainError(s"failed to parse string as Transaction string: $v")
        )
    }
  }

  case object Buy extends TraSide {
    override val v: String       = "buy"
    override val isBuy: Boolean  = true
    override val isSell: Boolean = false
  }

  case object Sell extends TraSide {
    override val v: String       = "sell"
    override val isBuy: Boolean  = false
    override val isSell: Boolean = true
  }
}
