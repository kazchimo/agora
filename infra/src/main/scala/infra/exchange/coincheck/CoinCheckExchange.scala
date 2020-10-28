package infra.exchange.coincheck

import java.nio.charset.StandardCharsets

import cats.syntax.traverse._
import eu.timepit.refined.auto._
import domain.exchange.{Exchange, Transaction}
import infra.InfraError
import io.circe.generic.auto._
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

final case class CoinCheckExchange(conf: CoinCheckExchangeConfig)
    extends Exchange.Service
    with AuthStrategy {
  def transactions: IO[Throwable, Seq[Transaction]] = {
    val url                             = "https://coincheck.com/api/exchange/orders/transactions"
    // ignore by-name implicit conversion warning
    // see -> https://users.scala-lang.org/t/2-13-3-by-name-implicit-linting-error/6334/2
    @nowarn def request(header: Header) = basicRequest
      .get(uri"$url")
      .headers(header)
      .response(
        asJson[TransactionsResponse]
          .mapRight(_.transactions.traverse(_.transformInto[Task[Transaction]]))
      )

    for {
      hs   <- headers(url)
      req   = request(hs)
      res  <- AsyncHttpClientZioBackend.managed().use(req.send(_))
      ress <- res.body.sequence.rightOrFail(InfraError("failed to request"))
    } yield ress
  }
}

private[exchange] trait AuthStrategy { self: CoinCheckExchange =>
  protected val encodeManner = "hmacSHA256"
  type Header = Map[String, String]

  protected def headers(url: String): IO[Throwable, Header] =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig   <- createSig(conf.secretKey.value, url, nonce)
    } yield Map(
      "ACCESS-KEY"       -> conf.apiKey.value.value,
      "ACCESS-NONCE"     -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = (System.currentTimeMillis() / 1000).toString

  private def createSig(secretKey: String, url: String, nonce: String) =
    hmacSHA256Encode(secretKey, nonce + url)

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
