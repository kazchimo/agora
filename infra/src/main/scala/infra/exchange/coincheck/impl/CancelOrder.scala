package infra.exchange.coincheck.impl
import domain.exchange.coincheck.CCOrder.CCOrderId
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  CancelOrderResponse,
  FailedCancelOrderResponse
}
import lib.error.ClientInfraError
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import sttp.client3.{UriContext, basicRequest}
import zio.{RIO, ZIO}

private[coincheck] trait CancelOrder extends AuthStrategy {
  self: CoinCheckExchangeImpl =>

  final override def cancelOrder(id: CCOrderId): RIO[SttpClient, CCOrderId] =
    for {
      h    <- headers(Endpoints.cancelOrder(id))
      req   = basicRequest
                .get(uri"${Endpoints.cancelOrder(id)}").contentType(
                  "application/json"
                ).headers(h).response(asJson[CancelOrderResponse])
      res  <- send(req)
      body <- ZIO.fromEither(res.body)
      _    <- body match {
                case FailedCancelOrderResponse(error) =>
                  ZIO.fail(ClientInfraError(s"Cancel Order failed: $error"))
                case _                                => ZIO.unit
              }
    } yield id
}
