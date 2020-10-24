package example

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import zio.ZIO

import scala.util.Try

case class CoincheckApi(apiKey: String, apiSecret: String) {
  private val Url = "https://coincheck.com/api/exchange/orders/transactions"
  private val AccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .mapError(_ => "CC_ACCESS_KEY not found")
  private val SecretKey = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .mapError(_ => "CC_SECRET_KEY not found")
  private val encodeManner = "hmacSHA256"

  def transactions(): ZIO[Any, String, String] =
    for {
      hs <- headers
      request = basicRequest.get(uri"$Url").headers(hs)
      res <- ZIO.fromEither(request.send(HttpURLConnectionBackend()).body)
    } yield res

  private def headers =
    for {
      accessKey <- AccessKey
      secKey <- SecretKey
      nonce = createNonce
      sig <- createSig(secKey, Url, nonce)
    } yield Map(
      "ACCESS-KEY" -> accessKey,
      "ACCESS-NONCE" -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = (System.currentTimeMillis() / 1000).toString

  private def createSig(secretKey: String, url: String, nonce: String) =
    hmacSHA256Encode(secretKey, url + nonce)

  def hmacSHA256Encode(
      secretKey: String,
      message: String
  ): ZIO[Any, String, String] =
    ZIO
      .fromTry(Try {
        val keySpec = new SecretKeySpec(
          secretKey.getBytes(StandardCharsets.UTF_8),
          encodeManner
        )
        val mac = Mac.getInstance(encodeManner)
        mac.init(keySpec)
        Hex.encodeHexString(
          mac.doFinal(message.getBytes(StandardCharsets.UTF_8))
        )
      })
      .mapError {
        case e: NoSuchAlgorithmException => s"wrong algorithm: ${e.getMessage}"
        case e: InvalidKeyException      => s"invalid key: ${e.getMessage}"
      }
}
