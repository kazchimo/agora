package infra.exchange.coincheck.impl

import java.nio.charset.StandardCharsets

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import zio.{Task, ZIO}

private[exchange] trait AuthStrategy { self: CoinCheckExchangeImpl =>
  protected val encodeManner = "hmacSHA256"
  type Header = Map[String, String]

  final protected def headers(url: String, body: String = ""): Task[Header] =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig   <- createSig(secretKey.value.value, url, nonce, body)
    } yield Map(
      "ACCESS-KEY"       -> accessKey.value.value,
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
