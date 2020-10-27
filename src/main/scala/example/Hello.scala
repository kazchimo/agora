package example

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import zio.ZIO
import zio.console._

case class CoincheckApi(accessKey: String, apiSecret: String) {
  private val Url = "https://coincheck.com/api/exchange/orders/transactions"

  private val encodeManner = "hmacSHA256"

  def transactions(): ZIO[Console, String, String] =
    for {
      hs     <- headers
      _      <- putStrLn(hs.toString())
      request = basicRequest.get(uri"$Url").headers(hs)
      res    <- ZIO.fromEither(request.send(HttpURLConnectionBackend()).body)
    } yield res

  private def headers =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig   <- createSig(apiSecret, Url, nonce)
    } yield Map(
      "ACCESS-KEY"       -> accessKey,
      "ACCESS-NONCE"     -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = (System.currentTimeMillis() / 1000).toString

  private def createSig(secretKey: String, url: String, nonce: String) =
    hmacSHA256Encode(secretKey, nonce + url)

  def hmacSHA256Encode(
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

