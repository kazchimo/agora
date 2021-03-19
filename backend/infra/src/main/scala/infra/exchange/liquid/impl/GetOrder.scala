package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{Id, Price, Quantity, Status}
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import infra.exchange.liquid.Endpoints
import infra.exchange.liquid.response.OrderResponse
import lib.refined.{PositiveDouble, PositiveLong}
import lib.syntax.all._
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.{IO, RIO, ZIO}
import io.circe.generic.auto._
import io.circe.refined._
import lib.error.ClientDomainError

trait GetOrder extends AuthRequest { self: LiquidExchange.Service =>
  private def url(id: Id) = Endpoints.ordersPath + s"/${id.deepInnerV.toString}"

  override def getOrder(id: Id): RIO[AllEnv, LiquidOrder] = for {
    req     <- authRequest(url(id))
    res     <- send(req.get(uri"${url(id)}").response(asJson[OrderResponse]))
    content <- ZIO.fromEither(res.body)
    order   <- content.toOrder
  } yield order
}
