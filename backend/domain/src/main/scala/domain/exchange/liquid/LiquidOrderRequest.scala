package domain.exchange.liquid

import domain.exchange.liquid.FundingCurrency.Jpy
import domain.exchange.liquid.LiquidOrder.OrderType.Limit
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.TradingType.Cfd
import domain.exchange.liquid.LiquidOrder._
import eu.timepit.refined.auto._

// https://developers.liquid.com/#create-an-order
final case class LiquidOrderRequest[O <: OrderType, S <: Side] private (
  orderType: O,
  productId: LiquidProduct.Id,
  side: S,
  quantity: Quantity,
  price: Option[Price] = None,
  leverageLevel: Option[LeverageLevel] = None,
  fundingCurrency: Option[FundingCurrency] = None,
  tradingType: Option[TradingType] = None,
  takeProfit: Option[TakeProfit] = None,
  stopLoss: Option[StopLoss] = None
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

  def leveraged[S <: Side](
    productId: LiquidProduct.Id,
    side: S,
    quantity: Quantity,
    price: Price,
    leverageLevel: LeverageLevel = LeverageLevel.unsafeApply(2L),
    takeProfit: TakeProfit,
    stopLoss: StopLoss
  ): LiquidOrderRequest[Limit.type, S] = LiquidOrderRequest(
    Limit,
    productId,
    side,
    quantity,
    Some(price),
    Some(leverageLevel),
    Some(Jpy),
    Some(Cfd),
    Some(takeProfit),
    Some(stopLoss)
  )
}
