package infra.exchange.coincheck.impl
import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  CancelOrderResponse,
  FailedCancelOrderResponse
}
import lib.error.{ClientInfraError, InternalInfraError}
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.circe.asJson
import zio.logging.{Logging, log}
import zio.{RIO, ZEnv, ZIO}

private[coincheck] trait CancelOrder extends AuthStrategy {
  self: CoincheckExchange.Service =>

  final override def cancelOrder(
    id: CCOrderId
  ): RIO[SttpClient with Logging with Conf with ZEnv, CCOrderId] = for {
    h    <- headers(Endpoints.cancelOrder(id))
    req   = jsonRequest
              .delete(uri"${Endpoints.cancelOrder(id)}").headers(h).response(
                asJson[CancelOrderResponse]
              )
    res  <- send(req).tap(a => log.trace(a.toString))
    body <- ZIO
              .fromEither(res.body).mapError(e =>
                InternalInfraError("Cancel Order failed", Some(e))
              )
    _    <- body match {
              case FailedCancelOrderResponse(error) =>
                ZIO.fail(ClientInfraError(s"Cancel Order failed: $error"))
              case _                                => ZIO.unit
            }
  } yield id
}
