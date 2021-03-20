package domain.exchange.liquid

import domain.exchange.liquid.LiquidOrder.OrderType.Limit
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder._

// https://developers.liquid.com/#create-an-order
final case class LiquidOrderRequest[O <: OrderType, S <: Side] private (
  orderType: O,
  productId: LiquidProduct.Id,
  side: S,
  quantity: Quantity,
  price: Option[Price] = None
)

object LiquidOrderRequest {
  def limitBuy(
    productId: LiquidProduct.Id,
    quantity: Quantity,
    price: Price
  ): LiquidOrderRequest[OrderType.Limit, Buy] =
    LiquidOrderRequest(Limit, productId, Buy, quantity, Some(price))

  def limitSell(
    productId: LiquidProduct.Id,
    quantity: Quantity,
    price: LiquidOrder.Price
  ): LiquidOrderRequest[OrderType.Limit, Sell] =
    LiquidOrderRequest(Limit, productId, Sell, quantity, Some(price))
}
