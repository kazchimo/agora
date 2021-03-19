package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder._
import domain.lib.{VOFactory, ZEnum}
import enumeratum.Enum
import enumeratum.EnumEntry.{Lowercase, Snakecase}
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveDouble

final case class LiquidOrder(price: Price, quantity: Quantity)

object LiquidOrder {
  @newtype case class Price(value: PositiveDouble)
  object Price extends VOFactory

  @newtype case class Quantity(value: PositiveDouble)
  object Quantity extends VOFactory

  sealed trait OrderType extends Snakecase
  object OrderType       extends ZEnum[OrderType] {
    override def values: IndexedSeq[OrderType] = findValues

    case object Limit           extends OrderType
    case object Market          extends OrderType
    case object MarketWithRange extends OrderType
    case object TrailingStop    extends OrderType
    case object LimitPostOnly   extends OrderType
    case object Stop            extends OrderType
  }

  sealed trait Side extends Lowercase
  object Side       extends Enum[Side] {
    override def values: IndexedSeq[Side] = findValues

    case object Buy  extends Side
    case object Sell extends Side
  }
}
