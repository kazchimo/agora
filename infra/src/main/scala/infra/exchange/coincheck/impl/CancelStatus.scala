package infra.exchange.coincheck.impl
import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  CancelStatusResponse,
  FailedCancelStatusResponse,
  SuccessCancelStatusResponse
}
import lib.error.ClientInfraError
import lib.sttp.jsonRequest
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.client3.{UriContext, basicRequest}
import zio.{RIO, ZIO}

private[coincheck] trait CancelStatus extends AuthStrategy {
  self: CoincheckExchange.Service =>

  final override def cancelStatus(
    id: CCOrderId
  ): RIO[SttpClient with Conf, Boolean] = for {
    h      <- headers(Endpoints.cancelStatus(id))
    req     = jsonRequest
                .get(uri"${Endpoints.cancelStatus(id)}").headers(h).response(
                  asJson[CancelStatusResponse]
                )
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
