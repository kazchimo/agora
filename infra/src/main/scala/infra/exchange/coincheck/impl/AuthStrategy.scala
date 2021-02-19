package infra.exchange.coincheck.impl

import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import zio.{Task, ZIO}

private[coincheck] trait AuthStrategy { self: CoinCheckExchangeImpl =>
  type Header = Map[String, String]

  final protected def headers(url: String, body: String = ""): Task[Header] =
    for {
      nonce <- ZIO.effectTotal(createNonce)
      sig   <- hmacSHA256Encode(secretKey.value.value, nonce + url + body)

    } yield Map(
      "ACCESS-KEY"       -> accessKey.value.value,
      "ACCESS-NONCE"     -> nonce,
      "ACCESS-SIGNATURE" -> sig
    )

  private def createNonce = System.currentTimeMillis().toString
}
