package domain.exchange.coincheck

import domain.exchange.coincheck.CCMarketBuy.CCMarketBuyAmount
import domain.exchange.coincheck.CCOrder.{CCOrderAmount, CCOrderRate}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

sealed trait CCOrder
object CCOrder {
  @newtype case class CCOrderRate(value: Long Refined Positive)
  object CCOrderRate extends VOFactory[Long, Positive] {
    override type VO = CCOrderRate
  }

  @newtype case class CCOrderAmount(value: Double Refined Positive)
  object CCOrderAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderAmount
  }
}

final case class CCBuy(rate: CCOrderRate, amount: CCOrderAmount) extends CCOrder

final case class CCStopBuy(
  rate: CCOrderRate,
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrder

final case class CCSell(rate: CCOrderRate, amount: CCOrderAmount)
    extends CCOrder

final case class CCStopSell(
  rate: CCOrderRate,
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrder

final case class CCMarketBuy(marketBuyAmount: CCMarketBuyAmount) extends CCOrder

final case class CCMarketStopBuy(
  stopLossRate: CCOrderRate,
  marketBuyAmount: CCMarketBuyAmount
) extends CCOrder

object CCMarketBuy {
  @newtype case class CCMarketBuyAmount(value: Double Refined Positive)
  object CCMarketBuyAmount extends VOFactory[Double, Positive] {
    override type VO = CCMarketBuyAmount
  }
}

final case class CCMarketSell(amount: CCOrderAmount) extends CCOrder

final case class MarketStopSell(
  stopLossRate: CCOrderRate,
  amount: CCOrderAmount
) extends CCOrder
