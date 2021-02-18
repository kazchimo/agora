package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder
import domain.exchange.coincheck.CCOrder.CCOrderId
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  FailedOpenOrdersResponse,
  OpenOrdersResponse,
  SuccessOpenOrdersResponse
}
import lib.error.ClientInfraError
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.client3.{UriContext, basicRequest}
import zio.{RIO, ZIO}

private[coincheck] trait OpenOrders extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  final override def openOrders: RIO[SttpClient, Seq[CCOrder]] = for {
    h      <- headers(Endpoints.openOrders)
    req     = basicRequest
                .get(uri"${Endpoints.openOrders}").contentType(
                  "application/json"
                ).headers(h).response(asJson[OpenOrdersResponse])
    res    <- send(req)
    body   <- ZIO.fromEither(res.body)
    orders <- body match {
                case r: SuccessOpenOrdersResponse  =>
                  ZIO.foreach(r.orders)(o => CCOrderId(o.id).map(CCOrder(_)))
                case FailedOpenOrdersResponse(err) =>
                  ZIO.fail(ClientInfraError(s"Request failed: $err"))
              }
  } yield orders
}
