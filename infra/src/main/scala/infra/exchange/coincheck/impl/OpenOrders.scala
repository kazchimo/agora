package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.{CCOpenOrder, CoincheckExchange}
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  FailedOpenOrdersResponse,
  OpenOrdersResponse,
  SuccessOpenOrdersResponse
}
import lib.error.ClientInfraError
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.{RIO, ZIO}

private[coincheck] trait OpenOrders extends AuthStrategy {
  self: CoincheckExchange.Service =>
  final override def openOrders: RIO[AllEnv, Seq[CCOpenOrder]] = (for {
    h      <- headers(Endpoints.openOrders)
    req     = jsonRequest
                .get(uri"${Endpoints.openOrders}").headers(h).response(
                  asJson[OpenOrdersResponse]
                )
    res    <- send(req)
    body   <- ZIO.fromEither(res.body)
    orders <- body match {
                case r: SuccessOpenOrdersResponse  => ZIO.foreach(r.orders)(o =>
                    CCOpenOrder.fromRaw(
                      o.id,
                      o.order_type,
                      o.rate,
                      o.pair,
                      o.pending_amount,
                      o.pending_market_buy_amount,
                      o.stop_loss_rate,
                      o.created_at
                    )
                  )
                case FailedOpenOrdersResponse(err) =>
                  ZIO.fail(ClientInfraError(s"Request failed: $err"))
              }
  } yield orders).retryN(3)
}
