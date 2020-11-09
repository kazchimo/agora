package infra.exchange.bitflyer.bodyconverter
import domain.exchange.bitflyer.BFChildOrder.{
  BFOrderMinuteToExpire,
  BFOrderPrice,
  BFOrderSize
}
import domain.exchange.bitflyer.{BFChildOrderType, BFOrderSide}
import io.circe.Encoder
import io.circe.syntax._

object BFChildOrderConverter {
  implicit val bfOrderPriceEncoder: Encoder[BFOrderPrice] =
    Encoder.instance(_.v.value.asJson)

  implicit val bfOrderSizeEncoder: Encoder[BFOrderSize] =
    Encoder.instance(_.v.value.asJson)

  implicit val BFOrderMinuteToExpireEncoder: Encoder[BFOrderMinuteToExpire] =
    Encoder.instance(_.v.value.asJson)

  implicit val BFOrderSideEncoder: Encoder[BFOrderSide] =
    Encoder.instance(_.v.asJson)

  implicit val BFChildOrderTypeEncoder: Encoder[BFChildOrderType] =
    Encoder.instance(_.v.asJson)
}
