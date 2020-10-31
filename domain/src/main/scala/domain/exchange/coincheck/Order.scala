package domain.exchange.coincheck

import domain.exchange.coincheck.MarketBuy.MarketBuyAmount
import domain.exchange.coincheck.Order.{OrderAmount, OrderRate}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

sealed trait Order
object Order {
  @newtype case class OrderRate(value: Long Refined Positive)
  object OrderRate extends VOFactory[Long, Positive] {
    override type VO = OrderRate
  }

  @newtype case class OrderAmount(value: Double Refined Positive)
  object OrderAmount extends VOFactory[Double, Positive] {
    override type VO = OrderAmount
  }
}

final case class Buy(rate: OrderRate, amount: OrderAmount) extends Order

final case class StopBuy(
  rate: OrderRate,
  stopLossRate: OrderRate,
  amount: OrderAmount
) extends Order

final case class Sell(rate: OrderRate, amount: OrderAmount) extends Order

final case class StopSell(
  rate: OrderRate,
  stopLossRate: OrderRate,
  amount: OrderAmount
) extends Order

final case class MarketBuy(marketBuyAmount: MarketBuyAmount) extends Order

final case class MarketStopBuy(
  stopLossRate: OrderRate,
  marketBuyAmount: MarketBuyAmount
) extends Order

object MarketBuy {
  @newtype case class MarketBuyAmount(value: Double Refined Positive)
  object MarketBuyAmount extends VOFactory[Double, Positive] {
    override type VO = MarketBuyAmount
  }
}

final case class MarketSell(amount: OrderAmount) extends Order

final case class MarketStopSell(stopLossRate: OrderRate, amount: OrderAmount)
    extends Order
