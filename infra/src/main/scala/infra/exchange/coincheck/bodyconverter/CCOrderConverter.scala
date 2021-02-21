package infra.exchange.coincheck.bodyconverter

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

  implicit def encodeOrderRequest[T]: Encoder[CCOrderRequest[T]] =
    Encoder.instance(_.asJson)

  implicit val ccOrderRequestRateEncoder: Encoder[CCOrderRequestRate] =
    Encoder.instance(_.value.value.asJson)

  implicit val ccOrderRequestAmountEncoder: Encoder[CCOrderRequestAmount] =
    Encoder.instance(_.value.value.asJson)
}
