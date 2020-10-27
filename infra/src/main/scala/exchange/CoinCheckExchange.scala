package exchange

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import eu.timepit.refined.auto._
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import io.estatico.newtype.macros.newtype
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import zio.{Has, ZIO}

final case class CoinCheckExchangeConfig(
  apiKey: CCEApiKey,
  secretKey: CCESecretKey
) extends ExchangeConfig[CoinCheckExchange]

object CoinCheckExchangeConfig {
  @newtype final case class CCEApiKey(value: NonEmptyString)
  @newtype final case class CCESecretKey(value: NonEmptyString)
}

final case class CoinCheckExchange()
    extends IExchange[CoinCheckExchange]
    with AuthStrategy {
  override type Conf = CoinCheckExchangeConfig

  def transactions: ZIO[Has[Conf], String, String] = for {
    url    <-
      ZIO.effectTotal("https://coincheck.com/api/exchange/orders/transactions")
    refUrl <- ZIO.fromEither(refineV[NonEmpty](url))
    hs     <- headers(refUrl)
    request = basicRequest.get(uri"$url").headers(hs)
    res    <- ZIO.fromEither(request.send(HttpURLConnectionBackend()).body)
  } yield res

}

private[exchange] trait AuthStrategy { self: CoinCheckExchange =>
  protected val encodeManner = "hmacSHA256"

  protected def headers(
    url: NonEmptyString
  ): ZIO[Has[Conf], String, Map[String, String]] =
    for {
      nonce  <- ZIO.effectTotal(createNonce)
      config <- ZIO.access[Has[Conf]](_.get)
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
