package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.Status.Filled
import domain.exchange.liquid.LiquidOrder._
import domain.lib.{VOFactory, ZEnum}
import enumeratum.Enum
import enumeratum.EnumEntry.{Lowercase, Snakecase}
import io.estatico.newtype.macros.newtype
import lib.enumeratum.GenericCirceEnum
import lib.refined.{PositiveDouble, PositiveLong}

final case class LiquidOrder(
  id: Id,
  price: Price,
  quantity: Quantity,
  status: Status
) {
  def filled: Boolean = status == Filled

  def notFilled: Boolean = !filled
}

final case class OrderOnBook(price: Price, quantity: Quantity)

object LiquidOrder {
  @newtype case class Id(value: PositiveLong)
  object Id extends VOFactory

  @newtype case class Price(value: PositiveDouble)
  object Price extends VOFactory

  @newtype case class Quantity(value: PositiveDouble)
  object Quantity extends VOFactory

  sealed trait OrderType extends Snakecase

  /** Marker trait that specify a order price. */
  sealed trait Pricable

  object OrderType extends ZEnum[OrderType] with GenericCirceEnum[OrderType] {
    override def values: IndexedSeq[OrderType] = findValues

    case object Limit           extends OrderType with Pricable
    case object Market          extends OrderType
    case object MarketWithRange extends OrderType with Pricable
    case object TrailingStop    extends OrderType
    case object LimitPostOnly   extends OrderType with Pricable
    case object Stop            extends OrderType with Pricable

    type Limit = Limit.type
  }

  sealed trait Side extends Lowercase
  object Side       extends Enum[Side] with GenericCirceEnum[Side] {
    override def values: IndexedSeq[Side] = findValues

    case object Buy  extends Side
    case object Sell extends Side

    type Buy  = Buy.type
    type Sell = Sell.type
  }

  sealed trait Status extends Snakecase
  object Status       extends ZEnum[Status] with GenericCirceEnum[Status] {
    override def values: IndexedSeq[Status] = findValues

    case object Live            extends Status
    case object Filled          extends Status
    case object PartiallyFilled extends Status
    case object Cancelled       extends Status
  }
}
