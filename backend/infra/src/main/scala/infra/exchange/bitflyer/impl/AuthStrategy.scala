package infra.exchange.bitflyer.impl

import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import lib.syntax.all._
import zio.{Task, ZIO}

private[bitflyer] trait AuthStrategy { self: BitflyerExchangeImpl =>
  type Header = Map[String, String]

  final def headers(method: String, path: String, body: String): Task[Header] =
    for {
      ts   <- ZIO.effectTotal(timestamp)
      sign <- hmacSHA256Encode(secretKey.deepInnerV, ts + method + path + body)
    } yield Map(
      "ACCESS-KEY"       -> accessKey.deepInnerV,
      "ACCESS-TIMESTAMP" -> ts,
      "ACCESS-SIGN"      -> sign
    )

  private def timestamp = (System.currentTimeMillis() / 1000).toString
}
