package infra.exchange.coincheck.impl

import cats.syntax.traverse._
import domain.exchange.coincheck.CCTransaction
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.TransactionsResponse
import io.scalaland.chimney.dsl._
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{send, SttpClient}
import sttp.client3.circe.asJson
import zio.{RIO, Task}
import zio.interop.catz.core._

import scala.annotation.nowarn

private[exchange] trait Transactions extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  // ignore by-name implicit conversion warning
  // see -> https://users.scala-lang.org/t/2-13-3-by-name-implicit-linting-error/6334/2
  @nowarn private def request(header: Header) = basicRequest
    .get(uri"${Endpoints.transactions}")
    .headers(header)
    .response(
      asJson[TransactionsResponse]
        .mapRight(_.transformInto[Task[List[CCTransaction]]])
    )

  final def transactions: RIO[SttpClient, Seq[CCTransaction]] =
    for {
      hs   <- headers(Endpoints.transactions)
      req   = request(hs)
      res  <- send(req)
      ress <- res.body.sequence.rightOrFail(InfraError("failed to request"))
    } yield ress
}
