package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.{OrderType, Quantity}
import domain.exchange.liquid.LiquidOrder.OrderType.Limit
import domain.exchange.liquid.LiquidOrder.Side.Buy

// https://developers.liquid.com/#create-an-order
final case class LiquidOrderRequest[O <: OrderType] private (
  orderType: O,
  productId: LiquidProduct.Id,
  side: LiquidOrder.Side,
  quantity: LiquidOrder.Quantity,
  price: Option[LiquidOrder.Price] = None
)

object LiquidOrderRequest {
  def limitBuy(
    product: LiquidProduct,
    quantity: Quantity,
    price: LiquidOrder.Price
  ): LiquidOrderRequest[OrderType.Limit] =
    LiquidOrderRequest(Limit, product.id, Buy, quantity, Some(price))
}
