package exchange

import currency.Currency
import exchange.Transaction.{TraCreatedAt, TraId, TraRate, TraSide}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import zio.{IO, ZIO}
import cats.syntax.either._
import domain.DomainError

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
    def apply(v: Long): IO[String, TraId] =
      ZIO.fromEither(refineV[Positive](v)).map(TraId(_))
  }

  @newtype case class TraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  object TraCreatedAt {
    def apply(v: String): IO[String, TraCreatedAt] =
      ZIO.fromEither(refineV[NonEmpty](v)).map(TraCreatedAt(_))
  }

  @newtype case class TraRate(value: Double Refined Positive)
  object TraRate {
    def apply(v: Double): IO[String, TraRate] =
      ZIO.fromEither(refineV[Positive](v)).map(TraRate(_))
  }

  sealed trait TraSide { val v: String }
  object TraSide       {
    def apply(v: String): IO[DomainError, TraSide] = v match {
      case Buy.v  => ZIO.succeed(Buy)
      case Sell.v => ZIO.succeed(Sell)
      case _      =>
        ZIO.fail(
          DomainError(s"failed to parse string as Transaction string: $v")
        )
    }
  }

  case object Buy  extends TraSide {
    override val v: String = "buy"
  }
  case object Sell extends TraSide {
    override val v: String = "sell"
  }
}
