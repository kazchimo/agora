package infra.exchange.liquid.impl

import domain.AllEnv
import domain.conf.Conf
import domain.exchange.liquid.errors.NotEnoughBalance
import io.circe.syntax._
import lib.sttp.jsonRequest
import lib.syntax.all._
import pdi.jwt.{Jwt, JwtAlgorithm}
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.{Effect, WebSockets}
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.{Empty, Request, RequestT}
import zio.logging.log
import zio.{RIO, Task, ZIO}

private[liquid] trait AuthRequest {
  def createSig(path: String): ZIO[AllEnv, Throwable, String] = for {
    tokenId <- Conf.liquidTokenId
    nonce    = System.currentTimeMillis
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

  private object ShouldRetry extends Exception

  def recover401Send[L <: Throwable, R](
    req: Request[Either[L, R], Effect[Task] with ZioStreams with WebSockets]
  ): ZIO[AllEnv, Throwable, R] = (for {
    _       <- log.debug(req.body.show)
    res     <- send(req)
    _       <- ZIO.fail(ShouldRetry).when(res.code.code == 401)
    _       <- ZIO.fail(NotEnoughBalance).when(res.code.code == 422)
    _       <- log.debug(res.show())
    content <- ZIO.fromEither(res.body)
  } yield content).retryWhileEquals(ShouldRetry)
}
