package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.{OrderType, Quantity}
import domain.exchange.liquid.LiquidOrder.OrderType.Limit
import domain.exchange.liquid.LiquidOrder.Side.Buy

// https://developers.liquid.com/#create-an-order
final case class LiquidOrderRequest[
  O <: OrderType,
  S <: LiquidOrder.Side
] private (
  orderType: O,
  productId: LiquidProduct.Id,
  side: S,
  quantity: LiquidOrder.Quantity,
  price: Option[LiquidOrder.Price] = None
)

object LiquidOrderRequest {
  def limitBuy(
    productId: LiquidProduct.Id,
    quantity: Quantity,
    price: LiquidOrder.Price
  ): LiquidOrderRequest[OrderType.Limit, Buy] =
    LiquidOrderRequest(Limit, productId, Buy, quantity, Some(price))
}
