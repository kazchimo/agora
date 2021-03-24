package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.Status.Filled
import domain.exchange.liquid.LiquidOrder._
import domain.lib.{VOFactory, ZEnum}
import enumeratum.Enum
import enumeratum.EnumEntry.{Lowercase, Snakecase}
import io.estatico.newtype.macros.newtype
import lib.enumeratum.GenericCirceEnum
import lib.error.ClientDomainError
import lib.refined.{PositiveDouble, PositiveLong}
import zio.{IO, ZIO}

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

  @newtype case class LeverageLevel private (value: PositiveLong)

  object LeverageLevel {
    def unsafeApply(value: PositiveLong): LeverageLevel = LeverageLevel(value)

    def create(value: PositiveLong): IO[ClientDomainError, LeverageLevel] = ZIO
      .fail(
        ClientDomainError(
          "Liquid leverage_level should be one of 2, 4, 5, 10, 25"
        )
      ).unless(Seq(2, 4, 5, 10, 25).contains(value.value)).as(
        LeverageLevel(value)
      )
  }

  sealed trait OrderDirection extends Snakecase
  object OrderDirection       extends ZEnum[OrderDirection] {
    override def values: IndexedSeq[OrderDirection] = findValues

    case object OneDirection extends OrderDirection
    case object TwoDirection extends OrderDirection
    case object Netout       extends OrderDirection
  }

  sealed trait TradingType extends Snakecase
  object TradingType       extends ZEnum[TradingType] {
    override def values: IndexedSeq[TradingType] = findValues

    case object Cfd       extends TradingType
    case object Margin    extends TradingType
    case object Perpetual extends TradingType
  }

  @newtype case class TakeProfit(value: PositiveDouble)
  object TakeProfit extends VOFactory

  @newtype case class StopLoss(value: PositiveDouble)
  object StopLoss extends VOFactory

  sealed trait MarginType extends Snakecase
  object MarginType       extends ZEnum[MarginType] {
    override def values: IndexedSeq[MarginType] = findValues

    case object Cross    extends MarginType
    case object Isolated extends MarginType
  }
}
