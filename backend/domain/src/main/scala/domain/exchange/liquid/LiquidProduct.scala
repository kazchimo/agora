package domain.exchange.liquid

import domain.exchange.liquid.LiquidProduct.{
  LastTradedPrice,
  LastTradedQuantity
}
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.refined.NonNegativeDouble

final case class LiquidProduct(
  lastTradedPrice: LastTradedPrice,
  lastTradedQuantity: LastTradedQuantity
)

object LiquidProduct {
  @newtype case class LastTradedPrice(value: NonNegativeDouble)
  object LastTradedPrice extends VOFactory

  @newtype case class LastTradedQuantity(value: NonNegativeDouble)
  object LastTradedQuantity extends VOFactory
}
