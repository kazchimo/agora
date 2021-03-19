package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{OrderType, Side}
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest}
import infra.exchange.liquid.Endpoints
import io.circe.{Encoder, Json}
import io.circe.generic.extras.Configuration
import sttp.client3.asynchttpclient.zio.{send, sendR}
import sttp.model.Uri
import zio.RIO
import io.circe.syntax._
import io.circe.refined._
import lib.instance.all._
import CreateOrder._
import sttp.client3.circe.asJson

private[liquid] trait CreateOrder extends AuthRequest {
  self: LiquidExchange.Service =>

  override def createOrder[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S]
  ): RIO[AllEnv, Unit] = for {
    req <- authRequest(Endpoints.ordersPath)
    _   <- send(
             req
               .post(Uri(Endpoints.orders)).body(
                 orderRequest.asJson.noSpaces
               ).response(asJson[Map[String, String]])
           )
  } yield ()
}

private[liquid] object CreateOrder {
  implicit def orderRequestEncoder[O <: OrderType, S <: Side]
    : Encoder[LiquidOrderRequest[O, S]] = Encoder.instance(req =>
    Json
      .obj(
        "order_type" -> req.orderType.asJson,
        "product_id" -> req.productId.asJson,
        "side"       -> req.side.asJson,
        "quantity"   -> req.quantity.asJson,
        "price"      -> req.price.asJson
      ).dropNullValues
  )
}
