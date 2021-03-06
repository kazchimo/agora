package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.{CCBalance, CoincheckExchange}
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.{
  BalanceResponse,
  FailedBalanceResponse,
  SuccessBalanceResponse
}
import lib.error.{ClientInfraError, InternalInfraError}
import lib.sttp.jsonRequest
import sttp.client3.UriContext
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.{RIO, ZIO}

private[coincheck] trait Balance extends AuthStrategy {
  self: CoincheckExchange.Service =>
  final override def balance: RIO[AllEnv, CCBalance] = for {
    h       <- headers(Endpoints.balance)
    req      = jsonRequest
                 .get(uri"${Endpoints.balance}").headers(h).response(
                   asJson[BalanceResponse]
                 )
    res     <- send(req)
    body    <- ZIO
                 .fromEither(res.body).mapError(e =>
                   InternalInfraError("Get Balance failed", Some(e))
                 )
    balance <- body match {
                 case FailedBalanceResponse(error)     =>
                   ZIO.fail(ClientInfraError(s"Get Balance failed: $error"))
                 case SuccessBalanceResponse(jpy, btc) =>
                   CCBalance.fromRaw(jpy, btc)
               }
  } yield balance
}
