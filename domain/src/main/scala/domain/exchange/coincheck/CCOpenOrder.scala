package domain.exchange.coincheck

import domain.exchange.coincheck.CCOpenOrder.CCOpenOrderType
import domain.exchange.coincheck.CCOrder.{
  CCOrderAmount,
  CCOrderCreatedAt,
  CCOrderId,
  CCOrderPair,
  CCOrderRate
}
import enumeratum._
import enumeratum.EnumEntry.Snakecase

final case class CCOpenOrder(
  id: CCOrderId,
  orderType: CCOpenOrderType,
  rate: Option[CCOrderRate],
  pair: CCOrderPair,
  pendingAmount: Option[CCOrderAmount],
  pendingMarketBuyAmount: Option[CCOrderAmount],
  stopLossRate: Option[CCOrderRate],
  createdAt: CCOrderCreatedAt
)

object CCOpenOrder {
  sealed trait CCOpenOrderType extends Snakecase

  object CCOpenOrderType extends Enum[CCOpenOrderType] {
    override def values: IndexedSeq[CCOpenOrderType] = findValues

    case object Buy  extends CCOpenOrderType
    case object Sell extends CCOpenOrderType
  }
}
