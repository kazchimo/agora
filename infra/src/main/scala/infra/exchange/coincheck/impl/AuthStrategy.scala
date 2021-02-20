package infra.exchange.coincheck.impl

import domain.conf.Conf
import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import zio.{RIO, ZIO}

private[coincheck] trait AuthStrategy {
  type Header = Map[String, String]

  final protected def headers(
    url: String,
    body: String = ""
  ): RIO[Conf, Header]    = for {
    nonce <- ZIO.effectTotal(createNonce)
    conf  <- ZIO.service[Conf.Service]
    sec   <- conf.ccSecretKey
    sig   <- hmacSHA256Encode(sec.value.value, nonce + url + body)
    acc   <- conf.ccAccessKey
  } yield Map("ACCESS-KEY" -> acc.value.value, "ACCESS-NONCE" -> nonce, "ACCESS-SIGNATURE" -> sig)

  private def createNonce = System.currentTimeMillis().toString
}
