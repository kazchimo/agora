package lib.cripto

import java.nio.charset.StandardCharsets

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import zio.{Task, ZIO}

object HmacSha256Encode {
  def hmacSHA256Encode(secretKey: String, message: String): Task[String] =
    ZIO.effect {
      val keySpec = new SecretKeySpec(
        secretKey.getBytes(StandardCharsets.US_ASCII),
        "hmacSHA256"
      )
      val mac     = Mac.getInstance("hmacSHA256")
      mac.init(keySpec)
      Hex.encodeHexString(
        mac.doFinal(message.getBytes(StandardCharsets.US_ASCII))
      )
    }
}
