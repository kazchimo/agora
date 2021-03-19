package domain.exchange.liquid

import domain.exchange.liquid.LiquidExecution._
import domain.lib.{ZEnum, VOFactory}
import enumeratum._
import io.estatico.newtype.macros.newtype
import lib.refined.{NonNegativeDouble, NonNegativeLong}

final case class LiquidExecution(
  id: Id,
  quantity: Quantity,
  price: Price,
  takerSide: TakerSide,
  createdAt: CreatedAt
)

object LiquidExecution {
  @newtype case class Id(value: NonNegativeLong)
  object Id extends VOFactory

  @newtype case class Quantity(value: NonNegativeDouble)
  object Quantity extends VOFactory

  @newtype case class Price(value: NonNegativeDouble)
  object Price extends VOFactory

  sealed abstract class TakerSide(override val entryName: String)
      extends EnumEntry

  object TakerSide extends ZEnum[TakerSide] {
    val values: IndexedSeq[TakerSide] = findValues

    case object Sell extends TakerSide("sell")
    case object Buy  extends TakerSide("buy")
  }

  @newtype case class CreatedAt(value: NonNegativeLong)
  object CreatedAt extends VOFactory
}
