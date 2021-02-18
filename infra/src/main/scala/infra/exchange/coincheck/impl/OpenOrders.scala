package infra.exchange.coincheck.impl

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
  final override def openOrders: RIO[SttpClient, Unit] = for {
    h    <- headers(Endpoints.openOrders)
    req   = basicRequest
              .get(uri"${Endpoints.openOrders}").contentType(
                "application/json"
              ).headers(h).response(asJson[OpenOrdersResponse])
    res  <- send(req)
    body <- ZIO.fromEither(res.body)
    _    <- body match {
              case _: SuccessOpenOrdersResponse  => ZIO.unit
              case FailedOpenOrdersResponse(err) =>
                ZIO.fail(ClientInfraError(s"Request failed: $err"))
            }
  } yield ()
}
