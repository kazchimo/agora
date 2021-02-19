package domain.exchange.coincheck

import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyRequestAmount
import domain.exchange.coincheck.CCLimitOrderRequest.{
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

sealed abstract class CCLimitOrderRequest(
  val rate: CCOrderRequestRate,
  val amount: CCOrderRequestAmount
) extends CCOrderRequest {
  def changeAmount(amount: CCOrderRequestAmount): CCLimitOrderRequest

  def changeRate(rate: CCOrderRequestRate): CCLimitOrderRequest
}

object CCLimitOrderRequest {
  @newtype case class CCOrderRequestRate(value: Double Refined Positive)
  object CCOrderRequestRate extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestRate
  }

  @newtype case class CCOrderRequestAmount(value: Double Refined Positive)
  object CCOrderRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestAmount
  }
}

final case class CCLimitBuyRequest(
  override val rate: CCOrderRequestRate,
  override val amount: CCOrderRequestAmount
) extends CCLimitOrderRequest(rate, amount) {
  override def changeAmount(amount: CCOrderRequestAmount): CCLimitOrderRequest =
    this.copy(amount = amount)

  override def changeRate(rate: CCOrderRequestRate): CCLimitOrderRequest =
    this.copy(rate = rate)
}

final case class CCLimitStopBuyRequest(
  override val rate: CCOrderRequestRate,
  stopLossRate: CCOrderRequestRate,
  override val amount: CCOrderRequestAmount
) extends CCLimitOrderRequest(rate, amount) {
  override def changeAmount(amount: CCOrderRequestAmount): CCLimitOrderRequest =
    this.copy(amount = amount)

  override def changeRate(rate: CCOrderRequestRate): CCLimitOrderRequest =
    this.copy(rate = rate)
}

final case class CCLimitSellRequest(
  override val rate: CCOrderRequestRate,
  override val amount: CCOrderRequestAmount
) extends CCLimitOrderRequest(rate, amount) {
  override def changeAmount(amount: CCOrderRequestAmount): CCLimitOrderRequest =
    this.copy(amount = amount)

  override def changeRate(rate: CCOrderRequestRate): CCLimitOrderRequest =
    this.copy(rate = rate)
}

final case class CCLimitStopSellRequest(
  override val rate: CCOrderRequestRate,
  stopLossRate: CCOrderRequestRate,
  override val amount: CCOrderRequestAmount
) extends CCLimitOrderRequest(rate, amount) {
  override def changeAmount(amount: CCOrderRequestAmount): CCLimitOrderRequest =
    this.copy(amount = amount)

  override def changeRate(rate: CCOrderRequestRate): CCLimitOrderRequest =
    this.copy(rate = rate)
}

sealed trait CCMarketOrderRequest extends CCOrderRequest

final case class CCMarketBuyRequest(marketBuyAmount: CCMarketBuyRequestAmount)
    extends CCMarketOrderRequest

final case class CCMarketStopBuyRequest(
  stopLossRate: CCOrderRequestRate,
  marketBuyAmount: CCMarketBuyRequestAmount
) extends CCMarketOrderRequest

object CCMarketBuyRequest {
  @newtype case class CCMarketBuyRequestAmount(value: Double Refined Positive)
  object CCMarketBuyRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCMarketBuyRequestAmount
  }
}

final case class CCMarketSellRequest(amount: CCOrderRequestAmount)
    extends CCMarketOrderRequest

final case class CCMarketStopSellRequest(
  stopLossRate: CCOrderRequestRate,
  amount: CCOrderRequestAmount
) extends CCMarketOrderRequest
