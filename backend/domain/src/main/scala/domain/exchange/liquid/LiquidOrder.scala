package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder._
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveDouble

final case class LiquidOrder(price: Price, quantity: Quantity)

object LiquidOrder {
  @newtype case class Price(value: PositiveDouble)
  object Price extends VOFactory

  @newtype case class Quantity(value: PositiveDouble)
  object Quantity extends VOFactory
}
