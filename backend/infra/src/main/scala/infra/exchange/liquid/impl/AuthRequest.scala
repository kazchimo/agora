package infra.exchange.liquid.impl

import domain.AllEnv
import domain.conf.Conf
import domain.exchange.Nonce
import domain.exchange.Nonce.Nonce
import io.circe.syntax._
import lib.sttp.jsonRequest
import lib.syntax.all._
import pdi.jwt.{Jwt, JwtAlgorithm}
import sttp.client3.{Empty, RequestT}
import zio.{RIO, ZIO}

private[liquid] trait AuthRequest {
  def createSig(path: String): ZIO[AllEnv, Throwable, String] = for {
    nonce   <- Nonce.getNonce
    tokenId <- Conf.liquidTokenId
    secret  <- Conf.liquidSecret
    payload  = Map(
                 "path"     -> path,
                 "nonce"    -> nonce.toString,
                 "token_id" -> tokenId.deepInnerV
               ).asJson.noSpaces
  } yield Jwt.encode(payload, secret.deepInnerV, JwtAlgorithm.HS256)

  def authRequest(
    path: String
  ): RIO[AllEnv, RequestT[Empty, Either[String, String], Any]] = createSig(path)
    .map(sig =>
      jsonRequest
        .header("X-Quoine-API-Version", "2").header("X-Quoine-Auth", sig)
    )
}
