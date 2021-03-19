package domain.exchange.liquid

import domain.exchange.liquid.LiquidProduct.{
  Id,
  LastTradedPrice,
  LastTradedQuantity
}
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.refined.{NonNegativeDouble, PositiveLong}

sealed abstract class LiquidProduct(
  val id: Id,
  val lastTradedPrice: LastTradedPrice,
  val lastTradedQuantity: LastTradedQuantity
)

object LiquidProduct {
  @newtype case class Id(value: PositiveLong)
  object Id extends VOFactory

  @newtype case class LastTradedPrice(value: NonNegativeDouble)
  object LastTradedPrice extends VOFactory

  @newtype case class LastTradedQuantity(value: NonNegativeDouble)
  object LastTradedQuantity extends VOFactory

}

final case class BtcJpyProduct(
  override val lastTradedPrice: LastTradedPrice,
  override val lastTradedQuantity: LastTradedQuantity
) extends LiquidProduct(Id.unsafeFrom(5L), lastTradedPrice, lastTradedQuantity)
