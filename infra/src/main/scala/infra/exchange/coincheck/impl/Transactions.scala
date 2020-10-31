package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCTransaction
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import infra.exchange.coincheck.responses.TransactionsResponse
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3._
import sttp.client3.circe.asJson
import io.circe.generic.auto._
import cats.syntax.traverse._
import io.scalaland.chimney.dsl._
import zio.{IO, Task}
import zio.interop.catz.core._

import scala.annotation.nowarn

private[exchange] trait Transactions extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  // ignore by-name implicit conversion warning
  // see -> https://users.scala-lang.org/t/2-13-3-by-name-implicit-linting-error/6334/2
  @nowarn def request(header: Header) = basicRequest
    .get(uri"${Endpoints.transactions}")
    .headers(header)
    .response(
      asJson[TransactionsResponse]
        .mapRight(_.transactions.traverse(_.transformInto[Task[CCTransaction]]))
    )

  def transactions: IO[Throwable, Seq[CCTransaction]] =
    for {
      hs   <- headers(Endpoints.transactions)
      req   = request(hs)
      res  <- AsyncHttpClientZioBackend.managed().use(req.send(_))
      ress <- res.body.sequence.rightOrFail(InfraError("failed to request"))
    } yield ress
}
