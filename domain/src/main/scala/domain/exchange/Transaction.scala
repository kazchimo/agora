package domain.exchange

import domain.DomainError
import domain.currency.Currency
import domain.exchange.Transaction.{TraCreatedAt, TraId, TraRate, TraSide}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.refined.refineVZE
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
  object TraId {
    def apply(v: Long): IO[Throwable, TraId] =
      refineVZE[Positive, Long](v).map(TraId(_))
  }

  @newtype case class TraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object TraCreatedAt {
    def apply(v: String): IO[Throwable, TraCreatedAt] =
      refineVZE[NonEmpty, String](v).map(TraCreatedAt(_))
  }

  @newtype case class TraRate(value: Double Refined Positive)
  object TraRate {
    def apply(v: Double): IO[Throwable, TraRate] =
      refineVZE[Positive, Double](v).map(TraRate(_))
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
