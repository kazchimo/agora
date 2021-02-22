package infra.exchange.coincheck.impl

import domain.conf.Conf
import domain.exchange.Nonce
import domain.exchange.Nonce.Nonce
import lib.cripto.HmacSha256Encode.hmacSHA256Encode
import lib.syntax.all._
import zio.{RIO, ZEnv, ZIO}

private[coincheck] trait AuthStrategy {
  type Header = Map[String, String]

  final protected def headers(
    url: String,
    body: String = ""
  ): RIO[Conf with ZEnv with Nonce, Header] = for {
    nonce <- Nonce.getNonce.map(_.toString)
    conf  <- ZIO.service[Conf.Service]
    sec   <- conf.ccSecretKey
    sig   <- hmacSHA256Encode(sec.deepInnerV, nonce + url + body)
    acc   <- conf.ccAccessKey
  } yield Map("ACCESS-KEY" -> acc.deepInnerV, "ACCESS-NONCE" -> nonce, "ACCESS-SIGNATURE" -> sig)
}
