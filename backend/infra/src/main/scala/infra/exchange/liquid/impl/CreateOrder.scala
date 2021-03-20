package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{OrderType, Side}
import domain.exchange.liquid.{LiquidExchange, LiquidOrder, LiquidOrderRequest}
import infra.exchange.liquid.Endpoints
import infra.exchange.liquid.response.OrderResponse
import io.circe.generic.auto._
import io.circe.refined._
import io.circe.syntax._
import io.circe.{Encoder, Json}
import lib.instance.all._
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.{RIO, ZIO}

import CreateOrder._

private[liquid] trait CreateOrder extends AuthRequest {
  self: LiquidExchange.Service =>

  override def createOrder[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S]
  ): RIO[AllEnv, LiquidOrder] = for {
    req   <- authRequest(Endpoints.ordersPath)
    res   <- recover401Send(
               req
                 .post(uri"${Endpoints.orders}").body(
                   orderRequest.asJson.noSpaces
                 ).response(asJson[OrderResponse])
             )
    order <- res.toOrder
  } yield order
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
