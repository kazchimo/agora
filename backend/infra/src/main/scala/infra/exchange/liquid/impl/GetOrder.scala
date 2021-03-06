package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.Id
import domain.exchange.liquid.errors.Unauthorized
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import infra.exchange.liquid.Endpoints
import infra.exchange.liquid.response.OrderResponse
import io.circe.generic.auto._
import io.circe.refined._
import lib.syntax.all._
import sttp.client3.UriContext
import sttp.client3.circe.asJson
import zio.RIO

trait GetOrder extends AuthRequest { self: LiquidExchange.Service =>
  private def url(id: Id) = Endpoints.ordersPath + s"/${id.deepInnerV.toString}"

  override def getOrder(id: Id): RIO[AllEnv, LiquidOrder] = (for {
    req   <- authRequest(url(id))
    uri    = Endpoints.root + url(id)
    res   <- asEitherSend(req.get(uri"$uri").response(asJson[OrderResponse]))
    order <- res.toOrder
  } yield order).retryWhile(_.isInstanceOf[Unauthorized])
}
