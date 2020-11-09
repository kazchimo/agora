package infra.exchange.bitflyer.impl

import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import zio.{Task, ZIO}

private[bitflyer] trait AuthStrategy { self: BitflyerExchangeImpl =>
  private type Header = Map[String, String]

  final def headers(method: String, path: String, body: String): Task[Header] =
    for {
      ts   <- ZIO.effectTotal(timestamp)
      sign <- hmacSHA256Encode(secretKey.value.value, ts + method + path + body)
    } yield Map(
      "ACCESS-KEY"       -> accessKey.value.value,
      "ACCESS-TIMESTAMP" -> ts,
      "ACCESS-SIGN"      -> sign
    )

  private def timestamp = (System.currentTimeMillis() / 1000).toString
}
