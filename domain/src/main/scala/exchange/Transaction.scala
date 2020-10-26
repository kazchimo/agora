package exchange

import currency.Currency
import exchange.Transaction.{TraCreatedAt, TraId, TraRate, TraSide}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

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
  @newtype case class TraCreatedAt(
    value: NonEmptyString
  ) // TODO: validate with iso date regex
  @newtype case class TraRate(value: Double Refined Positive)

  sealed trait TraSide
  case object Buy  extends TraSide
  case object Sell extends TraSide
}
