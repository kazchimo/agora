package infra.exchange.liquid.impl

import cats.syntax.traverse._
import domain.AllEnv
import domain.exchange.liquid.{GetTradesParams, LiquidExchange, Trade}
import infra.exchange.liquid.Endpoints
import infra.exchange.liquid.response.{PaginationContainer, TradeResponse}
import io.circe.generic.auto._
import io.circe.refined._
import lib.syntax.all._
import sttp.client3.UriContext
import sttp.client3.circe.asJson
import zio.RIO
import zio.interop.catz.core._
import zio.logging.log

private[liquid] trait GetTrades extends AuthRequest {
  self: LiquidExchange.Service =>
  private def url(params: GetTradesParams) = {
    val ps = Seq(
      params.fundingCurrency.map(f => s"funding_currency=${f.entryName}"),
      params.productId.map(p => s"product_id=${p.deepInnerV.toString}"),
      params.side.map(s => s"side=${s.entryName}"),
      params.status.map(s => s"status=${s.entryName}"),
      params.tradingType.map(s => s"tradingType=${s.entryName}"),
      params.limit.map(s => s"limit=${s.deepInnerV.toString}")
    ).collect { case Some(s) => s }

    "/trades" + (if (ps.isEmpty) "" else "?" + ps.mkString("&"))
  }

  override def getTrades(params: GetTradesParams): RIO[AllEnv, Seq[Trade]] =
    for {
      req    <- authRequest(url(params))
      uri     = Endpoints.root + url(params)
      res    <-
        recover401Send(
          req
            .get(uri"$uri").response(asJson[PaginationContainer[TradeResponse]])
        )
      _      <- log.debug(res.toString)
      trades <- res.models.map(_.toTrade).sequence
    } yield trades
}
