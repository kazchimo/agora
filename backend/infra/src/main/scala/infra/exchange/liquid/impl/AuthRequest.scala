package infra.exchange.liquid.impl

import domain.AllEnv
import domain.conf.Conf
import domain.exchange.Nonce
import io.circe.syntax._
import lib.sttp.jsonRequest
import lib.syntax.all._
import pdi.jwt.{Jwt, JwtAlgorithm}
import sttp.client3.{Empty, RequestT}
import zio.RIO

private[liquid] trait AuthRequest {
  def authRequest(
    path: String
  ): RIO[AllEnv, RequestT[Empty, Either[String, String], Any]] = for {
    nonce   <- Nonce.getNonce
    tokenId <- Conf.liquidTokenId
    secret  <- Conf.liquidSecret
    payload  = Map(
                 "path"     -> path,
                 "nonce"    -> nonce.toString,
                 "token_id" -> tokenId.deepInnerV
               ).asJson.noSpaces
    sig      = Jwt.encode(payload, secret.deepInnerV, JwtAlgorithm.HS256)
  } yield jsonRequest.header("X-Quoine-API-Version", "2").header("X-Quoine-Auth", sig)
}
