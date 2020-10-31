package infra.exchange.coincheck

import java.nio.charset.StandardCharsets

import cats.syntax.traverse._
import domain.exchange.coincheck.{CCOrder, CCTransaction, CoincheckExchange}
import eu.timepit.refined.auto._
import infra.InfraError
import infra.exchange.coincheck.bodyconverter.CCOrderConverter._
import infra.exchange.coincheck.responses.{OrdersResponse, TransactionsResponse}
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.circe._
import zio.interop.catz.core._
import zio.{IO, Task, ZIO}

import scala.annotation.nowarn

final case class CoinCheckExchangeImpl(conf: CoinCheckExchangeConfig)
    extends CoincheckExchange.Service
    with Transactions
    with Orders

private[exchange] trait Orders extends AuthStrategy {
  self: CoinCheckExchangeImpl =>
  @nowarn def request(order: CCOrder) = for {
    h <- headers(Endpoints.orders, order.asJson.noSpaces)
  } yield basicRequest
    .post(uri"${Endpoints.orders}")
    .contentType("application/json")
    .body(order.asJson.noSpaces)
    .headers(h)
    .response(asJson[OrdersResponse])

  def orders(order: CCOrder): Task[Unit] = for {
    req  <- request(order)
    _     = println(req.body)
    res  <- AsyncHttpClientZioBackend.managed().use(req.send(_))
    body <- ZIO.fromEither(res.body)
    r    <- if (body.success) Task.succeed(())
            else
              Task.fail(InfraError(s"failed to order: ${order.getClass.toString}"))
  } yield r

}

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

private[exchange] trait AuthStrategy { self: CoinCheckExchangeImpl =>
  protected val encodeManner = "hmacSHA256"
  type Header = Map[String, String]

  protected def headers(url: String, body: String = ""): IO[Throwable, Header] =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig   <- createSig(conf.secretKey.value, url, nonce, body)
    } yield Map(
      "ACCESS-KEY"       -> conf.apiKey.value.value,
      "ACCESS-NONCE"     -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = (System.currentTimeMillis() / 1000).toString

  private def createSig(
    secretKey: String,
    url: String,
    nonce: String,
    body: String
  ) =
    hmacSHA256Encode(secretKey, nonce + url + body)

  private def hmacSHA256Encode(
    secretKey: String,
    message: String
  ): Task[String] =
    ZIO
      .effect {
        val keySpec = new SecretKeySpec(
          secretKey.getBytes(StandardCharsets.US_ASCII),
          encodeManner
        )
        val mac     = Mac.getInstance(encodeManner)
        mac.init(keySpec)
        Hex.encodeHexString(
          mac.doFinal(message.getBytes(StandardCharsets.US_ASCII))
        )
      }

}
