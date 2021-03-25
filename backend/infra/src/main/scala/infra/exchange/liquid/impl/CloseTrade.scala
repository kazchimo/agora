package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, Trade}
import infra.exchange.liquid.Endpoints
import zio.RIO
import lib.syntax.all._
import sttp.client3.UriContext
import sttp.client3.circe.asJson

private[liquid] trait CloseTrade extends AuthRequest {
  self: LiquidExchange.Service =>
  override def closeTrade(id: Trade.Id): RIO[AllEnv, Unit] = {
    val path = s"/trades/${id.deepInnerV.toString}/close"
    val uri  = Endpoints.root + path

    for {
      req <- authRequest(path)
      _   <-
        recover401Send(req.put(uri"$uri").response(asJson[Map[String, String]]))
    } yield ()
  }
}
