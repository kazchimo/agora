package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidExchange
import infra.exchange.liquid.Endpoints
import io.circe.Json
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import sttp.client3.asynchttpclient.zio.sendR
import sttp.client3.{Response, UriContext, asWebSocketAlways, basicRequest}
import sttp.ws.{WebSocket, WebSocketClosed, WebSocketFrame}
import zio.logging.log
import zio.{RIO, ZIO}

private[liquid] case class WSMessage(event: String)
private[liquid] case class DataMessage(event: String, data: String)

private[liquid] trait WebSocketHandler extends AuthRequest {
  self: LiquidExchange.Service =>
  private def subscribeText(channel: String) = WebSocketFrame.text(
    Json
      .obj(
        "event" -> "pusher:subscribe".asJson,
        "data"  -> Json.obj("channel" -> channel.asJson)
      ).asJson.noSpaces
  )

  private def authText = for {
    sig <- createSig("/realtime")
    text = Json.obj(
             "event" -> "quoine:auth_request".asJson,
             "data"  -> Json.obj(
               "headers" -> Json.obj("X-Quoine-Auth" -> sig.asJson),
               "path"    -> "/realtime".asJson
             )
           )
  } yield WebSocketFrame.text(text.noSpaces)

  protected def handleMessage(
    ws: WebSocket[RIO[AllEnv, *]],
    subscribeChannel: String,
    eventName: String
  )(
    onEvent: DataMessage => ZIO[AllEnv, Throwable, Unit]
  ): ZIO[AllEnv, Throwable, Unit] = (for {
    msg  <- ws.receiveTextFrame()
    _    <- log.trace(msg.payload)
    data <- ZIO.fromEither(decode[WSMessage](msg.payload))
    _    <- data.event match {
              case "pusher:connection_established"          =>
                ws.send(subscribeText(subscribeChannel)) *> log.info("Connected!")
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case `eventName`                              =>
                ZIO.fromEither(decode[DataMessage](msg.payload)).flatMap(onEvent)
              case a                                        => log.warn(s"Unexpected ws response: $a")
            }
  } yield ()).retryWhile(_.isInstanceOf[WebSocketClosed])

  protected def handleAuthMessage(
    ws: WebSocket[RIO[AllEnv, *]],
    subscribeChannel: String,
    eventName: String
  )(
    onEvent: DataMessage => ZIO[AllEnv, Throwable, Unit]
  ): ZIO[AllEnv, Throwable, Unit] = (for {
    msg  <- ws.receiveTextFrame()
    _    <- log.trace(msg.payload)
    data <- ZIO.fromEither(decode[WSMessage](msg.payload))
    _    <- data.event match {
              case "pusher:connection_established"          => log.info("Connected!") *>
                  authText.flatMap(t => ws.send(t))
              case "quoine:auth_success"                    => log.info("Authenticated!") *> ws.send(
                  subscribeText(subscribeChannel)
                )
              case "quoine:auth_failure"                    => log.error("Auth failed!")
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case `eventName`                              =>
                ZIO.fromEither(decode[DataMessage](msg.payload)).flatMap(onEvent)
              case a                                        => log.warn(s"Unexpected ws response: $a")
            }
  } yield ()).retryWhile(_.isInstanceOf[WebSocketClosed])

  protected def sendWS(
    f: WebSocket[RIO[AllEnv, *]] => ZIO[AllEnv, Throwable, Unit]
  ): ZIO[AllEnv, Throwable, Response[Unit]] = sendR[Unit, AllEnv](
    basicRequest.get(uri"${Endpoints.ws}").response(asWebSocketAlways(f))
  )
}
