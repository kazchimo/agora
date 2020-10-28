package exchange.coincheck

import java.nio.charset.StandardCharsets

import eu.timepit.refined.auto._
import exchange.{Exchange, Transaction}
import io.circe.generic.auto._
import io.scalaland.chimney.dsl._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.circe._
import zio.{IO, Task, ZIO}

final case class CoinCheckExchange(conf: CoinCheckExchangeConfig)
    extends Exchange.Service
    with AuthStrategy {
  def transactions: IO[Throwable, Seq[Transaction]] = {
    val url                     = "https://coincheck.com/api/exchange/orders/transactions"
    def request(header: Header) = basicRequest
      .get(uri"$url")
      .headers(header)
      .response(
        asJson[Seq[TransactionsResponse]]
          .mapRight(_.map(_.transformInto[Transaction]))
      )

    for {
      hs   <- headers(url)
      req   = request(hs)
      res  <- AsyncHttpClientZioBackend.managed().use(req.send(_))
      ress <- ZIO.fromEither(res.body)
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
