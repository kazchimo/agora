package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCOrder, CoincheckExchange}
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  FailedOpenOrdersResponse,
  OpenOrdersResponse,
  SuccessOpenOrdersResponse
}
import lib.error.ClientInfraError
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import zio.{RIO, ZEnv, ZIO}

private[coincheck] trait OpenOrders extends AuthStrategy {
  self: CoincheckExchange.Service =>
  final override def openOrders
    : RIO[SttpClient with Conf with ZEnv with Nonce, Seq[CCOrder]] = (for {
    h      <- headers(Endpoints.openOrders)
    req     = jsonRequest
                .get(uri"${Endpoints.openOrders}").headers(h).response(
                  asJson[OpenOrdersResponse]
                )
    res    <- send(req)
    body   <- ZIO.fromEither(res.body)
    orders <- body match {
                case r: SuccessOpenOrdersResponse  =>
                  ZIO.foreach(r.orders)(o => CCOrderId(o.id).map(CCOrder(_)))
                case FailedOpenOrdersResponse(err) =>
                  ZIO.fail(ClientInfraError(s"Request failed: $err"))
              }
  } yield orders).retryN(3)
}
