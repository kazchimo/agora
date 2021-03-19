package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.OrderType

// https://developers.liquid.com/#create-an-order
final case class LiquidOrderRequest[O <: OrderType] private (
  orderType: O,
  productId: LiquidProduct.Id,
  side: LiquidOrder.Side,
  quantity: Option[LiquidOrder.Quantity]
)

object LiquidOrderRequest {}
