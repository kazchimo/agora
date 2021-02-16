package domain.exchange.coincheck

import domain.exchange.coincheck.CCPublicTransaction._
import eu.timepit.refined.api.Refined
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
  @newtype final case class CCPubTraId(value: NonEmptyString)

  @newtype final case class CCPubTraPair(value: NonEmptyString)

  @newtype final case class CCPubTraRate(value: Refined[Double, Positive])

  @newtype final case class CCPubTraQuantity(value: Refined[Double, Positive])

  sealed abstract class CCPubTraSide(val value: String)
  object CCPubTraBuy  extends CCPubTraSide("buy")
  object CCPubTraSell extends CCPubTraSide("sell")
}
