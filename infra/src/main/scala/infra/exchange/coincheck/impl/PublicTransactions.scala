package infra.exchange.coincheck.impl

import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{SttpClient, sendR}
import sttp.ws.WebSocket
import zio._
import zio.console.putStrLn
import zio.stream.Stream

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  private def useWS(que: Queue[String])(ws: WebSocket[RIO[ZEnv, *]]) = {
    val send    =
      ws.sendText("{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}")
    val receive = for {
      textEither <- ws.receiveTextFrame()
      _          <- putStrLn(s"received: ${textEither.toString}")
      text       <-
        ZIO
          .fromEither(textEither)
          .onError(_ => putStrLn("shutdown") *> que.shutdown)
          .mapError(c =>
            InfraError(
              s"close websocket connection: statusCode=${c.statusCode.toString} reason=${c.reasonText}"
            )
          )
      _          <- putStrLn(text.toString)
      _          <- que.offer(text.payload)
    } yield ()

    send *> putStrLn("send websocket") *> receive.forever
  }

  final override def publicTransactions
    : ZIO[SttpClient with ZEnv, Throwable, Stream[Nothing, String]] = for {
    _   <- putStrLn("start public transactions")
    que <- Queue.unbounded[String]
    _   <- sendR[Unit, ZEnv](
             basicRequest
               .get(uri"${Endpoints.websocket}")
               .response(asWebSocketAlways(useWS(que)))
           ).fork
    _   <- putStrLn("send complete")
  } yield Stream.fromQueueWithShutdown(que).interruptWhen(que.awaitShutdown)
}
