package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, Trade}
import infra.exchange.liquid.Endpoints
import infra.exchange.liquid.response.TradeResponse
import io.circe.generic.auto._
import io.circe.refined._
import lib.syntax.all._
import sttp.client3.UriContext
import sttp.client3.circe.asJson
import zio.RIO

private[liquid] trait CloseTrade extends AuthRequest {
  self: LiquidExchange.Service =>
  override def closeTrade(id: Trade.Id): RIO[AllEnv, Unit] = {
    val path = s"/trades/${id.deepInnerV.toString}/close"
    val uri  = Endpoints.root + path

    for {
      req <- authRequest(path)
      _   <- recoverUnauthorizedSend(
               req.put(uri"$uri").response(asJson[TradeResponse])
             )
    } yield ()
  }
}
