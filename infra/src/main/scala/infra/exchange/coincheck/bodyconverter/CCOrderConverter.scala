package infra.exchange.coincheck.bodyconverter

import domain.exchange.coincheck.CCOrder.{
  CCOrderAmount,
  CCOrderRate,
  CCOrderType
}
import domain.exchange.coincheck._
import io.circe.generic.extras.Configuration
import io.circe.syntax._
import io.circe.{Encoder, Json}
import lib.syntax.all._

object CCOrderConverter {
  implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit def encodeOrderRequest[T <: CCOrderType]
    : Encoder[CCOrderRequest[T]] = Encoder.instance { req =>
    Json
      .obj(
        "pair"              -> req.pair.asJson,
        "order_type"        -> req.orderType.asJson,
        "rate"              -> req.rate.asJson,
        "amount"            -> req.amount.asJson,
        "market_buy_amount" -> req.marketBuyAmount.asJson,
        "stop_loss_rate"    -> req.stopLossRate.asJson
      ).dropNullValues
  }

  implicit def encodeOrderType[T <: CCOrderType]: Encoder[T] =
    Encoder.instance(_.entryName.asJson)

  implicit val ccOrderRequestRateEncoder: Encoder[CCOrderRate] =
    Encoder.instance(_.deepInnerV.asJson)

  implicit val ccOrderRequestAmountEncoder: Encoder[CCOrderAmount] =
    Encoder.instance(_.deepInnerV.asJson)
}
