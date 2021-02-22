package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderPair.BtcJpy
import domain.exchange.coincheck.CCOrder.CCOrderType.{
  Buy,
  MarketBuy,
  MarketSell,
  Sell
}
import domain.exchange.coincheck.CCOrder.{CCOrderPair, CCOrderType, LimitOrder}
import domain.exchange.coincheck.CCOrderRequest._
import domain.lib.VOFactory
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.error.ClientDomainError
import lib.refined.PositiveDouble
import lib.syntax.all._
import zio.IO

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

final case class CCOrderRequest[+T <: CCOrderType] private (
  pair: CCOrderPair,
  orderType: T,
  rate: Option[CCOrderRequestRate] = None,
  amount: Option[CCOrderRequestAmount] = None,
  marketBuyAmount: Option[CCOrderRequestAmount] = None, // NOTE: JPY amount
  stopLossRate: Option[CCOrderRequestRate] = None
) {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitRate[S >: T: <:<[*, LimitOrder]]: CCOrderRequestRate = rate.get

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitAmount[S >: T: <:<[*, LimitOrder]]: CCOrderRequestAmount = amount.get

  def changeRate[S >: T](
    rate: CCOrderRequestRate
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(rate = Some(rate), orderType = ev(orderType))

  def changeAmount[S >: T](
    amount: CCOrderRequestAmount
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(amount = Some(amount), orderType = ev(orderType))

  def jpy[S >: T: =:=[*, LimitOrder]]: Double =
    limitAmount.deepInnerV * limitRate.deepInnerV
}

private[coincheck] trait OrderFactory {
  final def unsafeApply[T <: CCOrderType](
    pair: CCOrderPair,
    orderType: T,
    rate: Option[CCOrderRequestRate],
    amount: Option[CCOrderRequestAmount],
    marketBuyAmount: Option[CCOrderRequestAmount],
    stopLossRate: Option[CCOrderRequestRate]
  ): CCOrderRequest[T] =
    CCOrderRequest(pair, orderType, rate, amount, marketBuyAmount, stopLossRate)

  final def limitBuy(
    rate: Double,
    amount: Double
  ): IO[ClientDomainError, CCOrderRequest[Buy]] = CCOrderRequestRate(rate)
    .zip(CCOrderRequestAmount(amount)).map(a => limitBuy(a._1, a._2))

  final def limitBuy(
    rate: CCOrderRequestRate,
    amount: CCOrderRequestAmount
  ): CCOrderRequest[Buy] = CCOrderRequest(BtcJpy, Buy, Some(rate), Some(amount))

  final def limitSell(
    rate: Double,
    amount: Double
  ): IO[ClientDomainError, CCOrderRequest[Sell]] = CCOrderRequestRate(rate)
    .zip(CCOrderRequestAmount(amount)).map(a => limitSell(a._1, a._2))

  final def limitSell(
    rate: CCOrderRequestRate,
    amount: CCOrderRequestAmount
  ): CCOrderRequest[Sell] =
    CCOrderRequest(BtcJpy, Sell, Some(rate), Some(amount))

  final def marketBuy(
    marketBuyAmount: Double
  ): IO[ClientDomainError, CCOrderRequest[MarketBuy]] =
    CCOrderRequestAmount(marketBuyAmount).map(marketBuy)

  final def marketBuy(
    marketBuyAmount: CCOrderRequestAmount
  ): CCOrderRequest[MarketBuy] =
    CCOrderRequest(BtcJpy, MarketBuy, marketBuyAmount = Some(marketBuyAmount))

  final def marketSell(
    amount: Double
  ): IO[ClientDomainError, CCOrderRequest[MarketSell]] =
    CCOrderRequestAmount(amount).map(marketSell)

  final def marketSell(
    amount: CCOrderRequestAmount
  ): CCOrderRequest[MarketSell] =
    CCOrderRequest(BtcJpy, MarketSell, amount = Some(amount))
}

object CCOrderRequest extends OrderFactory {

  @newtype case class CCOrderRequestRate(value: PositiveDouble)
  object CCOrderRequestRate extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestRate
  }

  @newtype case class CCOrderRequestAmount(value: PositiveDouble)
  object CCOrderRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestAmount
  }
}
