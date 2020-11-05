package infra.exchange.coincheck.impl

import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import zio.{Task, ZIO}

private[exchange] trait AuthStrategy { self: CoinCheckExchangeImpl =>
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

}
