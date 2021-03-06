package infra.exchange.coincheck.impl

import cats.syntax.traverse._
import domain.AllEnv
import domain.exchange.coincheck.{CCTransaction, CoincheckExchange}
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.TransactionsResponse
import io.scalaland.chimney.dsl._
import sttp.client3._
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.circe.asJson
import zio.interop.catz.core._
import zio.{RIO, Task}

private[exchange] trait Transactions extends AuthStrategy {
  self: CoincheckExchange.Service =>
  private def request(header: Header) = basicRequest
    .get(uri"${Endpoints.transactions}")
    .headers(header)
    .response(
      asJson[TransactionsResponse]
        .mapRight(_.transformInto[Task[List[CCTransaction]]])
    )

  final def transactions: RIO[AllEnv, Seq[CCTransaction]] = (for {
    hs   <- headers(Endpoints.transactions)
    req   = request(hs)
    res  <- send(req)
    ress <- res.body.sequence.rightOrFailWith((e: Throwable) =>
              InfraError(s"failed to request: ${e.toString}")
            )
  } yield ress).retryN(3)
}
