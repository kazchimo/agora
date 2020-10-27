package exchange

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import exchange.CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import io.estatico.newtype.macros.newtype
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.{IO, ZIO}

final case class CoinCheckExchangeConfig(
  apiKey: CCEApiKey,
  secretKey: CCESecretKey
)

object CoinCheckExchangeConfig {
  @newtype final case class CCEApiKey(value: NonEmptyString)
  object CCEApiKey {
    def apply(value: String): IO[String, CCEApiKey] =
      ZIO.fromEither(refineV[NonEmpty](value)).map(CCEApiKey(_))
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey {
    def apply(value: String): IO[String, CCESecretKey] =
      ZIO.fromEither(refineV[NonEmpty](value)).map(CCESecretKey(_))
  }
}

final case class CoinCheckExchange(conf: CoinCheckExchangeConfig)
    extends Exchange.Service
    with AuthStrategy {
  def transactions: IO[String, String] = for {
    url    <-
      ZIO.effectTotal("https://coincheck.com/api/exchange/orders/transactions")
    refUrl <- ZIO.fromEither(refineV[NonEmpty](url))
    hs     <- headers(refUrl)
    res    <- AsyncHttpClientZioBackend.managed().mapError(_.getMessage).use { backend =>
                basicRequest
                  .get(uri"$url")
                  .headers(hs)
                  .send(backend)
                  .mapError(_.getMessage)
              }
    ress   <- ZIO.fromEither(res.body)
  } yield ress
}

private[exchange] trait AuthStrategy { self: CoinCheckExchange =>
  protected val encodeManner = "hmacSHA256"

  protected def headers(url: NonEmptyString): IO[String, Map[String, String]] =
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
  ): IO[String, String] =
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
      .mapError {
        case e: NoSuchAlgorithmException => s"wrong algorithm: ${e.getMessage}"
        case e: InvalidKeyException      => s"invalid key: ${e.getMessage}"
      }
}
