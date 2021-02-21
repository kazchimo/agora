package infra.exchange.bitflyer.bodyconverter
import domain.exchange.bitflyer.BFChildOrder.{
  BFOrderMinuteToExpire,
  BFOrderPrice,
  BFOrderSize
}
import lib.syntax.all._
import domain.exchange.bitflyer._
import io.circe.Encoder
import io.circe.syntax._

object BFChildOrderConverter {
  implicit val bfOrderPriceEncoder: Encoder[BFOrderPrice] =
    Encoder.instance(_.deepInnerV.asJson)

  implicit val bfOrderSizeEncoder: Encoder[BFOrderSize] =
    Encoder.instance(_.deepInnerV.asJson)

  implicit val BFOrderMinuteToExpireEncoder: Encoder[BFOrderMinuteToExpire] =
    Encoder.instance(_.deepInnerV.asJson)

  implicit val BFOrderSideEncoder: Encoder[BFOrderSide] =
    Encoder.instance(_.v.asJson)

  implicit val BFChildOrderTypeEncoder: Encoder[BFChildOrderType] =
    Encoder.instance(_.v.asJson)

  implicit val bfTimeInForceEncoder: Encoder[BFQuantityConditionsEnforcement] =
    Encoder.instance(_.v.asJson)

  implicit val bfProductCodeEncoder: Encoder[BFProductCode] =
    Encoder.instance(_.code.asJson)

  implicit val bfChildOrderEncoder: Encoder[BFChildOrder] = Encoder.forProduct7(
    "product_code",
    "child_order_type",
    "side",
    "price",
    "size",
    "minute_to_expire",
    "time_in_force"
  )(o =>
    (
      o.productCode.asJson,
      o.tpe.asJson,
      o.side.asJson,
      o.price.asJson,
      o.size.asJson,
      o.minuteToExpire.asJson,
      o.timeInForce.asJson
    )
  )
}
