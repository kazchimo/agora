package exchange.coincheck

import java.nio.charset.StandardCharsets

import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import exchange.Exchange
import infra.InfraError
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.circe._
import io.circe.generic.auto._
import zio.{IO, Task, ZIO}

final case class CoinCheckExchange(conf: CoinCheckExchangeConfig)
    extends Exchange.Service
    with AuthStrategy {
  def transactions: IO[Throwable, Seq[TransactionsResponse]] = for {
    url    <-
      ZIO.effectTotal("https://coincheck.com/api/exchange/orders/transactions")
    refUrl <- ZIO.fromEither(refineV[NonEmpty](url)).mapError(InfraError)
    hs     <- headers(refUrl)
    req     = basicRequest
                .get(uri"$url")
                .headers(hs)
                .response(asJson[Seq[TransactionsResponse]])
    res    <- AsyncHttpClientZioBackend.managed().use(req.send(_))
    ress   <- ZIO.fromEither(res.body)
  } yield ress
}

private[exchange] trait AuthStrategy { self: CoinCheckExchange =>
  protected val encodeManner = "hmacSHA256"

  protected def headers(
    url: NonEmptyString
  ): IO[Throwable, Map[String, String]] =
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
