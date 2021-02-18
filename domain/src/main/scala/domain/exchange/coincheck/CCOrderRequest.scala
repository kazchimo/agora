package domain.exchange.coincheck

import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyRequestAmount
import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.lib.VOFactory
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

sealed trait CCOrderRequest
object CCOrderRequest {
  @newtype case class CCOrderRequestRate(value: Long Refined Positive)
  object CCOrderRequestRate extends VOFactory[Long, Positive] {
    override type VO = CCOrderRequestRate
  }

  @newtype case class CCOrderRequestAmount(value: Double Refined Positive)
  object CCOrderRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestAmount
  }
}

final case class CCBuyRequest(
  rate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCOrderRequest

final case class CCStopBuyRequest(
  rate: CCOrderRequestRate,
  stopLossRate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCOrderRequest

final case class CCSellRequest(
  rate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCOrderRequest

final case class CCStopSellRequest(
  rate: CCOrderRequestRate,
  stopLossRate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCOrderRequest

final case class CCMarketBuyRequest(marketBuyAmount: CCMarketBuyRequestAmount)
    extends CCOrderRequest

final case class CCMarketStopBuyRequest(
  stopLossRate: CCOrderRequestRate,
  marketBuyAmount: CCMarketBuyRequestAmount
) extends CCOrderRequest

object CCMarketBuyRequest {
  @newtype case class CCMarketBuyRequestAmount(value: Double Refined Positive)
  object CCMarketBuyRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCMarketBuyRequestAmount
  }
}

final case class CCMarketSellRequest(amount: CCOrderRequestAmount)
    extends CCOrderRequest

final case class CCMarketStopSellRequest(
  stopLossRate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCOrderRequest
