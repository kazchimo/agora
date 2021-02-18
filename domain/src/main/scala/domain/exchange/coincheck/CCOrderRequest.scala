package domain.exchange.coincheck

import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyAmount
import domain.exchange.coincheck.CCOrderRequest.{CCOrderAmount, CCOrderRate}
import domain.lib.VOFactory
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

sealed trait CCOrderRequest
object CCOrderRequest {
  @newtype case class CCOrderRate(value: Long Refined Positive)
  object CCOrderRate extends VOFactory[Long, Positive] {
    override type VO = CCOrderRate
  }

  @newtype case class CCOrderAmount(value: Double Refined Positive)
  object CCOrderAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderAmount
  }
}

final case class CCBuyRequest(rate: CCOrderRate, amount: CCOrderAmount)
    extends CCOrderRequest

final case class CCStopBuyRequest(
  rate: CCOrderRate,
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrderRequest

final case class CCSellRequest(rate: CCOrderRate, amount: CCOrderAmount)
    extends CCOrderRequest

final case class CCStopSellRequest(
  rate: CCOrderRate,
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrderRequest

final case class CCMarketBuyRequest(marketBuyAmount: CCMarketBuyAmount)
    extends CCOrderRequest

final case class CCMarketStopBuyRequest(
  stopLossRate: CCOrderRate,
  marketBuyAmount: CCMarketBuyAmount
) extends CCOrderRequest

object CCMarketBuyRequest {
  @newtype case class CCMarketBuyAmount(value: Double Refined Positive)
  object CCMarketBuyAmount extends VOFactory[Double, Positive] {
    override type VO = CCMarketBuyAmount
  }
}

final case class CCMarketSellRequest(amount: CCOrderAmount)
    extends CCOrderRequest

final case class CCMarketStopSellRequest(
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrderRequest
