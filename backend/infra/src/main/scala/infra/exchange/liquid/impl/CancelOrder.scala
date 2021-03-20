package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import infra.exchange.liquid.Endpoints
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.RIO
import cats.syntax.either._
import lib.error.InternalInfraError

trait CancelOrder extends AuthRequest { self: LiquidExchange.Service =>
  override def cancelOrder(id: LiquidOrder.Id): RIO[AllEnv, Unit] = for {
    req <- authRequest(Endpoints.cancelOrder(id))
    uri  = Endpoints.root + Endpoints.cancelOrder(id)
    _   <- recover401Send(
             req.put(uri"$uri").mapResponse(_.leftMap(e => InternalInfraError(e)))
           )
  } yield ()
}
