package infra.exchange.coincheck.bodyconverter

import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyRequestAmount
import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck._
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._

object CCOrderConverter {
  implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val encodeOrder: Encoder[CCOrderRequest] = Encoder.instance {
    case a: CCBuyRequest            => a.asJson
    case a: CCStopBuyRequest        => a.asJson
    case a: CCSellRequest           => a.asJson
    case a: CCStopSellRequest       => a.asJson
    case a: CCMarketBuyRequest      => a.asJson
    case a: CCMarketStopBuyRequest  => a.asJson
    case a: CCMarketSellRequest     => a.asJson
    case a: CCMarketStopSellRequest => a.asJson
  }

  private val orderType       = "order_type"
  private val rate            = "rate"
  private val amount          = "amount"
  private val marketBuyAmount = "market_buy_amount"
  private val stopLossRate    = "stop_loss_rate"
  private val pair            = "pair"
  private val btcJpy          = "btc_jpy"

  implicit val orderRateEncoder: Encoder[CCOrderRequestRate] =
    Encoder.instance(_.value.value.asJson)

  implicit val orderAmountEncoder: Encoder[CCOrderRequestAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val marketBuyAmountEncoder: Encoder[CCMarketBuyRequestAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val buyEncoder: Encoder[CCBuyRequest] =
    Encoder.forProduct4(pair, orderType, rate, amount)(a =>
      (btcJpy, "buy", a.rate, a.amount)
    )

  implicit val stopBuyEncoder: Encoder[CCStopBuyRequest] =
    Encoder.forProduct5(pair, orderType, rate, stopLossRate, amount)(a =>
      (btcJpy, "buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val sellEncoder: Encoder[CCSellRequest] =
    Encoder.forProduct4(pair, orderType, rate, amount)(a =>
      (btcJpy, "sell", a.rate, a.amount)
    )

  implicit val stopSellEncoder: Encoder[CCStopSellRequest] =
    Encoder.forProduct5(pair, orderType, rate, stopLossRate, amount)(a =>
      (btcJpy, "buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val encodeMarketBuy: Encoder[CCMarketBuyRequest] =
    Encoder.forProduct3(pair, orderType, marketBuyAmount)(a =>
      (btcJpy, "market_buy", a.marketBuyAmount)
    )

  implicit val encodeMarketStopBuy: Encoder[CCMarketStopBuyRequest] =
    Encoder.forProduct4(pair, orderType, stopLossRate, marketBuyAmount)(a =>
      (btcJpy, "market_buy", a.stopLossRate, a.marketBuyAmount)
    )

  implicit val encodeMarketSell: Encoder[CCMarketSellRequest] =
    Encoder.forProduct3(pair, orderType, amount)(a =>
      (btcJpy, "market_sell", a.amount)
    )

  implicit val encodeMarketStopSell: Encoder[CCMarketStopSellRequest] =
    Encoder.forProduct4(pair, orderType, stopLossRate, amount)(a =>
      (btcJpy, "market_sell", a.stopLossRate, a.amount)
    )
}
