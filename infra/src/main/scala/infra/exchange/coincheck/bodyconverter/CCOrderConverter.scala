package infra.exchange.coincheck.bodyconverter

import domain.exchange.coincheck.CCMarketBuy.CCMarketBuyAmount
import domain.exchange.coincheck.CCOrder.{CCOrderAmount, CCOrderRate}
import domain.exchange.coincheck._
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._

object CCOrderConverter {
  implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val encodeOrder: Encoder[CCOrder] = Encoder.instance {
    case a: CCBuy            => a.asJson
    case a: CCStopBuy        => a.asJson
    case a: CCSell           => a.asJson
    case a: CCStopSell       => a.asJson
    case a: CCMarketBuy      => a.asJson
    case a: CCMarketStopBuy  => a.asJson
    case a: CCMarketSell     => a.asJson
    case a: CCMarketStopSell => a.asJson
  }

  private val orderType       = "order_type"
  private val rate            = "rate"
  private val amount          = "amount"
  private val marketBuyAmount = "market_buy_amount"
  private val stopLossRate    = "stop_loss_rate"

  implicit val orderRateEncoder: Encoder[CCOrderRate] =
    Encoder.instance(_.value.value.asJson)

  implicit val orderAmountEncoder: Encoder[CCOrderAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val marketBuyAmountEncoder: Encoder[CCMarketBuyAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val buyEncoder: Encoder[CCBuy] =
    Encoder.forProduct3(orderType, rate, amount)(a => ("buy", a.rate, a.amount))

  implicit val stopBuyEncoder: Encoder[CCStopBuy] =
    Encoder.forProduct4(orderType, rate, stopLossRate, amount)(a =>
      ("buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val sellEncoder: Encoder[CCSell] =
    Encoder.forProduct3(orderType, rate, amount)(a =>
      ("sell", a.rate, a.amount)
    )

  implicit val stopSellEncoder: Encoder[CCStopSell] =
    Encoder.forProduct4(orderType, rate, stopLossRate, amount)(a =>
      ("buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val encodeMarketBuy: Encoder[CCMarketBuy] =
    Encoder.forProduct2(orderType, marketBuyAmount)(a =>
      ("market_buy", a.marketBuyAmount)
    )

  implicit val encodeMarketStopBuy: Encoder[CCMarketStopBuy] =
    Encoder.forProduct3(orderType, stopLossRate, marketBuyAmount)(a =>
      ("market_buy", a.stopLossRate, a.marketBuyAmount)
    )

  implicit val encodeMarketSell: Encoder[CCMarketSell] =
    Encoder.forProduct2(orderType, amount)(a => ("market_sell", a.amount))

  implicit val encodeMarketStopSell: Encoder[CCMarketStopSell] =
    Encoder.forProduct3(orderType, stopLossRate, amount)(a =>
      ("market_sell", a.stopLossRate, a.amount)
    )
}
