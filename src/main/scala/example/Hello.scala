package example

import java.nio.charset.StandardCharsets
import java.security.{InvalidKeyException, NoSuchAlgorithmException}

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex
import sttp.client3._
import zio.console._
import zio.{ExitCode, URIO, ZIO}

case class CoincheckApi(accessKey: String, apiSecret: String) {
  private val Url = "https://coincheck.com/api/exchange/orders/transactions"

  private val encodeManner = "hmacSHA256"

  def transactions(): ZIO[Console, String, String] =
    for {
      hs <- headers
      _ <- putStrLn(hs.toString())
      request = basicRequest.get(uri"$Url").headers(hs)
      res <- ZIO.fromEither(request.send(HttpURLConnectionBackend()).body)
    } yield res

  private def headers =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig <- createSig(apiSecret, Url, nonce)
    } yield Map(
      "ACCESS-KEY" -> accessKey,
      "ACCESS-NONCE" -> nonce,
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
        val keySpec = new SecretKeySpec(secretKey.getBytes(), encodeManner)
        val mac = Mac.getInstance(encodeManner)
        mac.init(keySpec)
        Hex.encodeHexString(mac.doFinal(message.getBytes()))
      }
      .mapError {
        case e: NoSuchAlgorithmException => s"wrong algorithm: ${e.getMessage}"
        case e: InvalidKeyException      => s"invalid key: ${e.getMessage}"
      }
}

object Main extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = app.exitCode

  private val AccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .mapError(_ => "CC_ACCESS_KEY not found")
  private val SecretKey = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .mapError(_ => "CC_SECRET_KEY not found")

  val app = for {
    accessKey <- AccessKey
    secKey <- SecretKey
    api = CoincheckApi(accessKey, secKey)
    _ <- putStrLn(accessKey)
    _ <- putStrLn(secKey)
    tra <- api.transactions()
    _ <- putStrLn(tra)
  } yield ()
}
