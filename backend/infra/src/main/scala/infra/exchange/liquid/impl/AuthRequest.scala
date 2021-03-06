package infra.exchange.liquid.impl

import domain.AllEnv
import domain.conf.Conf
import domain.exchange.liquid.errors._
import io.circe.syntax._
import lib.error.InternalInfraError
import lib.sttp.jsonRequest
import lib.syntax.all._
import pdi.jwt.{Jwt, JwtAlgorithm}
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.{Effect, WebSockets}
import sttp.client3.asynchttpclient.zio.send
import sttp.client3.{Empty, Request, RequestT, Response}
import sttp.model.Header
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

  def authHeaders(path: String): RIO[AllEnv, Seq[Header]] =
    createSig(path).map(sig =>
      Seq(Header("X-Quoine-API-Version", "2"), Header("X-Quoine-Auth", sig))
    )

  def authRequest(
    path: String
  ): RIO[AllEnv, RequestT[Empty, Either[String, String], Any]] =
    authHeaders(path).map(h => jsonRequest.headers(h: _*))

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
               case a   => InternalInfraError(
                   s"Unknown response code from Liquid api: code=$a body=${res.show()}"
                 )
             }
           }.when(res.code.isClientError || res.code.isServerError)
  } yield res

  def asEitherSend[L <: Throwable, R](
    req: Request[Either[L, R], Effect[Task] with ZioStreams with WebSockets]
  ): RIO[AllEnv, R] = for {
    res     <- sendReq(req)
    content <- ZIO.fromEither(res.body)
  } yield content
}
