package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.lib.VOFactory
import enumeratum.EnumEntry.Snakecase
import enumeratum.{CirceEnum, Enum}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveDouble

final case class CCOrder(id: CCOrderId)

object CCOrder {
  @newtype case class CCOrderId(value: Refined[Long, Positive])
  object CCOrderId extends VOFactory[Long, Positive] {
    override type VO = CCOrderId
  }

  sealed trait CCOrderType extends Snakecase
  sealed trait LimitOrder  extends CCOrderType
  sealed trait MarketOrder extends CCOrderType
  sealed trait BuySide     extends CCOrderType
  sealed trait SellSide    extends CCOrderType

  object CCOrderType extends Enum[CCOrderType] with CirceEnum[CCOrderType] {
    val values: IndexedSeq[CCOrderType] = findValues

    case object Buy        extends LimitOrder with BuySide
    case object Sell       extends LimitOrder with SellSide
    case object MarketBuy  extends MarketOrder with BuySide
    case object MarketSell extends MarketOrder with SellSide

    type Buy        = Buy.type
    type Sell       = Sell.type
    type MarketBuy  = MarketBuy.type
    type MarketSell = MarketSell.type
  }

  sealed trait CCOrderPair extends Snakecase
  object CCOrderPair       extends Enum[CCOrderPair] with CirceEnum[CCOrderPair] {
    val values: IndexedSeq[CCOrderPair] = findValues

    case object BtcJpy  extends CCOrderPair
    case object EtcJpy  extends CCOrderPair
    case object FctJpy  extends CCOrderPair
    case object MonaJpy extends CCOrderPair
  }

  @newtype case class CCOrderRate(value: PositiveDouble)
  object CCOrderRate extends VOFactory[Double, Positive] {
    override type VO = CCOrderRate
  }

  @newtype case class CCOrderAmount(value: PositiveDouble)
  object CCOrderAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderAmount
  }
}
