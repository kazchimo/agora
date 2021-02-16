package infra.exchange.coincheck.impl

import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{SttpClient, sendR}
import sttp.ws.WebSocket
import zio._
import zio.logging.{Logging, log}
import zio.stream.Stream

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  private def useWS(
    que: Queue[String]
  )(ws: WebSocket[RIO[ZEnv with Logging, *]]) = {
    val send    =
      ws.sendText("{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}")
    val receive = for {
      textEither <- ws.receiveTextFrame()
      text       <-
        ZIO
          .fromEither(textEither)
          .onError(_ => log.error("shutdown") *> que.shutdown)
          .mapError(c =>
            InfraError(
              s"close websocket connection: statusCode=${c.statusCode.toString} reason=${c.reasonText}"
            )
          )
      _          <- que.offer(text.payload)
    } yield ()

    send *> receive.forever
  }

  final override def publicTransactions: ZIO[
    SttpClient with ZEnv with Logging,
    Throwable,
    Stream[Nothing, String]
  ] = for {
    que <- Queue.unbounded[String]
    _   <- sendR[Unit, ZEnv with Logging](
             basicRequest
               .get(uri"${Endpoints.websocket}")
               .response(asWebSocketAlways(useWS(que)))
           ).fork
  } yield Stream.fromQueueWithShutdown(que).interruptWhen(que.awaitShutdown)
}
