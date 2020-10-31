package infra.exchange.coincheck.bodyconverter

import domain.exchange.coincheck.MarketBuy.MarketBuyAmount
import domain.exchange.coincheck.Order.{OrderAmount, OrderRate}
import domain.exchange.coincheck._
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import lib.circe.AnyValConverter

object OrderConverter extends AnyValConverter {
  implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val encodeOrder: Encoder[Order] = Encoder.instance {
    case a: Buy            => a.asJson
    case a: StopBuy        => a.asJson
    case a: Sell           => a.asJson
    case a: StopSell       => a.asJson
    case a: MarketBuy      => a.asJson
    case a: MarketStopBuy  => a.asJson
    case a: MarketSell     => a.asJson
    case a: MarketStopSell => a.asJson
  }

  private val orderType       = "order_type"
  private val rate            = "rate"
  private val amount          = "amount"
  private val marketBuyAmount = "market_buy_amount"
  private val stopLossRate    = "stop_loss_rate"

  implicit val orderRateEncoder: Encoder[OrderRate] =
    Encoder.instance(_.value.value.asJson)

  implicit val orderAmountEncoder: Encoder[OrderAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val marketBuyAmountEncoder: Encoder[MarketBuyAmount] =
    Encoder.instance(_.value.value.asJson)

  implicit val buyEncoder: Encoder[Buy] =
    Encoder.forProduct3(orderType, rate, amount)(a => ("buy", a.rate, a.amount))

  implicit val stopBuyEncoder: Encoder[StopBuy] =
    Encoder.forProduct4(orderType, rate, stopLossRate, amount)(a =>
      ("buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val sellEncoder: Encoder[Sell] =
    Encoder.forProduct3(orderType, rate, amount)(a =>
      ("sell", a.rate, a.amount)
    )

  implicit val stopSellEncoder: Encoder[StopSell] =
    Encoder.forProduct4(orderType, rate, stopLossRate, amount)(a =>
      ("buy", a.rate, a.stopLossRate, a.amount)
    )

  implicit val encodeMarketBuy: Encoder[MarketBuy] =
    Encoder.forProduct2(orderType, marketBuyAmount)(a =>
      ("market_buy", a.marketBuyAmount)
    )

  implicit val encodeMarketStopBuy: Encoder[MarketStopBuy] =
    Encoder.forProduct3(orderType, stopLossRate, marketBuyAmount)(a =>
      ("market_buy", a.stopLossRate, a.marketBuyAmount)
    )

  implicit val encodeMarketSell: Encoder[MarketSell] =
    Encoder.forProduct2(orderType, amount)(a => ("market_sell", a.amount))

  implicit val encodeMarketStopSell: Encoder[MarketStopSell] =
    Encoder.forProduct3(orderType, stopLossRate, amount)(a =>
      ("market_sell", a.stopLossRate, a.amount)
    )
}
