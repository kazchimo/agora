package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderPair.BtcJpy
import domain.exchange.coincheck.CCOrder.CCOrderType.{
  Buy,
  MarketBuy,
  MarketSell,
  Sell
}
import domain.exchange.coincheck.CCOrder._
import lib.error.ClientDomainError
import lib.syntax.all._
import zio.IO

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

final case class CCOrderRequest[+T <: CCOrderType] private (
  pair: CCOrderPair,
  orderType: T,
  rate: Option[CCOrderRate] = None,
  amount: Option[CCOrderAmount] = None,
  marketBuyAmount: Option[CCOrderAmount] = None, // NOTE: JPY amount
  stopLossRate: Option[CCOrderRate] = None
) {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitRate[S >: T: <:<[*, LimitOrder]]: CCOrderRate = rate.get

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitAmount[S >: T: <:<[*, LimitOrder]]: CCOrderAmount = amount.get

  def changeRate[S >: T](
    rate: CCOrderRate
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(rate = Some(rate), orderType = ev(orderType))

  def changeAmount[S >: T](
    amount: CCOrderAmount
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(amount = Some(amount), orderType = ev(orderType))

  def jpy[S >: T: =:=[*, LimitOrder]]: Double =
    limitAmount.deepInnerV * limitRate.deepInnerV
}

private[coincheck] trait OrderFactory {
  final def unsafeApply[T <: CCOrderType](
    pair: CCOrderPair,
    orderType: T,
    rate: Option[CCOrderRate],
    amount: Option[CCOrderAmount],
    marketBuyAmount: Option[CCOrderAmount],
    stopLossRate: Option[CCOrderRate]
  ): CCOrderRequest[T] =
    CCOrderRequest(pair, orderType, rate, amount, marketBuyAmount, stopLossRate)

  final def limitBuy(
    rate: CCOrderRate,
    amount: CCOrderAmount
  ): CCOrderRequest[Buy] = CCOrderRequest(BtcJpy, Buy, Some(rate), Some(amount))

  final def limitSell(
    rate: CCOrderRate,
    amount: CCOrderAmount
  ): CCOrderRequest[Sell] =
    CCOrderRequest(BtcJpy, Sell, Some(rate), Some(amount))

  final def marketBuy(
    marketBuyAmount: CCOrderAmount
  ): CCOrderRequest[MarketBuy] =
    CCOrderRequest(BtcJpy, MarketBuy, marketBuyAmount = Some(marketBuyAmount))

  final def marketSell(amount: CCOrderAmount): CCOrderRequest[MarketSell] =
    CCOrderRequest(BtcJpy, MarketSell, amount = Some(amount))
}

object CCOrderRequest extends OrderFactory
