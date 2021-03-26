package infra.exchange.liquid.impl

import domain.AllEnv
import domain.conf.Conf
import domain.exchange.liquid.errors.{
  BadRequest,
  ServiceUnavailable,
  TooManyRequests,
  Unauthorized,
  UnprocessableEntity
}
import io.circe.syntax._
import lib.sttp.jsonRequest
import lib.syntax.all._
import pdi.jwt.{Jwt, JwtAlgorithm}
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.{Effect, WebSockets}
import sttp.client3.asynchttpclient.zio.{SttpClient, send}
import sttp.client3.{Empty, Request, RequestT, Response}
import zio.logging.{Logging, log}
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

  def sendReq[T](
    req: Request[T, Effect[Task] with ZioStreams with WebSockets]
  ): RIO[AllEnv, Response[T]] = for {
    _   <- log.debug(s"Request: url=${req.uri.toString} body=${req.body.show}")
    res <- send(req)
    _   <- log.debug(s"Response: url=${req.uri.toString} body=${res.show()}")
    _   <- ZIO.fail {
             res.code.code match {
               case 400 => BadRequest(res.show())
               case 401 => Unauthorized(res.show())
               case 422 => UnprocessableEntity(res.show())
               case 429 => TooManyRequests
               case 503 => ServiceUnavailable(res.show())
             }
           }
  } yield res

  private object ShouldRetry extends Exception

  def recover401Send[L <: Throwable, R](
    req: Request[Either[L, R], Effect[Task] with ZioStreams with WebSockets]
  ): ZIO[AllEnv, Throwable, R] = (for {
    res     <- sendReq(req)
    content <- ZIO.fromEither(res.body)
  } yield content).retryWhileEquals(ShouldRetry)
}
