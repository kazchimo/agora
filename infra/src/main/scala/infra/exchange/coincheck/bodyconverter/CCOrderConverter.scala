package infra.exchange.coincheck.bodyconverter

import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderPair,
  CCOrderRequestAmount,
  CCOrderRequestRate,
  CCOrderType
}
import domain.exchange.coincheck._
import io.circe.Encoder
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import enumeratum._

object CCOrderConverter {
  implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit def encodeOrderRequest[T]: Encoder[CCOrderRequest[T]] =
    Encoder.instance(_.asJson)

//  implicitly[Encoder[CCOrderPair]]

  private val orderType       = "order_type"
  private val rate            = "rate"
  private val amount          = "amount"
  private val marketBuyAmount = "market_buy_amount"
  private val stopLossRate    = "stop_loss_rate"
  private val pair            = "pair"
  private val btcJpy          = "btc_jpy"

  implicit val ccOrderRequestRateEncoder: Encoder[CCOrderRequestRate] =
    Encoder.instance(_.value.value.asJson)

  implicit val ccOrderRequestAmountEncoder: Encoder[CCOrderRequestAmount] =
    Encoder.instance(_.value.value.asJson)
}
