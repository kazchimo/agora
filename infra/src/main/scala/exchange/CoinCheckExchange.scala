package exchange

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString
import exchange.CoinCheckExchangeConfig.{CCApiKey, CCSecretKey}
import io.estatico.newtype.macros.newtype
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import zio.ZIO

final case class CoinCheckExchangeConfig(
  apiKey: CCApiKey,
  secretKey: CCSecretKey
)

object CoinCheckExchangeConfig {
  @newtype final case class CCApiKey(value: NonEmptyString)
  @newtype final case class CCSecretKey(value: NonEmptyString)
}

final class CoinCheckExchange extends IExchange {
  private val encodeManner = "hmacSHA256"

  def transactions: ZIO[CoinCheckExchangeConfig, String, String] = for {
    url    <-
      ZIO.effectTotal("https://coincheck.com/api/exchange/orders/transactions")
    hs     <- headers(url)
    request = basicRequest.get(uri"$url").headers(hs)
    res    <- ZIO.fromEither(request.send(HttpURLConnectionBackend()).body)
  } yield res

  private def headers(url: NonEmptyString) =
    for {
      nonce  <- ZIO.effectTotal(createNonce)
      config <- ZIO.access[CoinCheckExchangeConfig].apply(identity)
      sig    <- createSig(config.secretKey.value, url, nonce)
    } yield Map(
      "ACCESS-KEY"       -> config.apiKey.value.value,
      "ACCESS-NONCE"     -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = (System.currentTimeMillis() / 1000).toString

  private def createSig(secretKey: String, url: String, nonce: String) =
    hmacSHA256Encode(secretKey, nonce + url)

  private def hmacSHA256Encode(
    secretKey: String,
    message: String
  ): ZIO[Any, String, String] =
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
