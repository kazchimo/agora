package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidExchange
import io.circe.Json
import sttp.ws.{WebSocket, WebSocketClosed, WebSocketFrame}
import zio.{IO, RIO, ZIO}
import zio.logging.log
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax._

private[liquid] case class WSMessage(event: String)

private[liquid] trait WebSocketHandler { self: LiquidExchange.Service =>
  private def subscribeText(channel: String) = WebSocketFrame.text(
    Json
      .obj(
        "event" -> "pusher:subscribe".asJson,
        "data"  -> Json.obj("channel" -> channel.asJson)
      ).asJson.noSpaces
  )

  def handleMessage(
    ws: WebSocket[RIO[AllEnv, *]],
    subscribeChannel: String,
    eventName: String
  )(
    onEvent: String => ZIO[AllEnv, Throwable, Unit]
  ): ZIO[AllEnv, Throwable, Unit] = (for {
    msg  <- ws.receiveTextFrame()
    _    <- log.trace(msg.payload)
    data <- ZIO.fromEither(decode[WSMessage](msg.payload))
    _    <- data.event match {
              case "pusher:connection_established"          =>
                ws.send(subscribeText(subscribeChannel)) *> log.info("Connected!")
              case "pusher_internal:subscription_succeeded" =>
                log.debug("Subscribed ws!")
              case `eventName`                              => onEvent(msg.payload)
              case a                                        => log.warn(s"Unexpected ws response: $a")
            }
  } yield ()).retryWhile(_.isInstanceOf[WebSocketClosed])
}