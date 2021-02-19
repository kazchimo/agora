package infra.exchange.coincheck.impl
import domain.exchange.coincheck.CCOrder.CCOrderId
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  CancelStatusResponse,
  FailedCancelStatusResponse,
  SuccessCancelStatusResponse
}
import lib.error.ClientInfraError
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.client3.{UriContext, basicRequest}
import zio.{RIO, ZIO}

private[coincheck] trait CancelStatus extends AuthStrategy {
  self: CoinCheckExchangeImpl =>

  final override def cancelStatus(id: CCOrderId): RIO[SttpClient, Boolean] =
    for {
      h      <- headers(Endpoints.cancelStatus(id))
      req     = basicRequest
                  .get(uri"${Endpoints.cancelStatus(id)}").contentType(
                    "application/json"
                  ).headers(h).response(asJson[CancelStatusResponse])
      res    <- send(req)
      body   <- ZIO.fromEither(res.body)
      status <- body match {
                  case SuccessCancelStatusResponse(_, cancel, _) =>
                    ZIO.succeed(cancel)
                  case FailedCancelStatusResponse(error)         =>
                    ZIO.fail(ClientInfraError(s"Cancel order failed: $error"))
                }
    } yield status
}