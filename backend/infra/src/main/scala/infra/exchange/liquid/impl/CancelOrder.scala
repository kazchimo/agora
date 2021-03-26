package infra.exchange.liquid.impl

import cats.syntax.either._
import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import infra.exchange.liquid.Endpoints
import lib.error.InternalInfraError
import sttp.client3.UriContext
import zio.RIO

trait CancelOrder extends AuthRequest { self: LiquidExchange.Service =>
  override def cancelOrder(id: LiquidOrder.Id): RIO[AllEnv, Unit] = for {
    req <- authRequest(Endpoints.cancelOrder(id))
    uri  = Endpoints.root + Endpoints.cancelOrder(id)
    _   <- recoverUnauthorizedSend(
             req.put(uri"$uri").mapResponse(_.leftMap(e => InternalInfraError(e)))
           )
  } yield ()
}
