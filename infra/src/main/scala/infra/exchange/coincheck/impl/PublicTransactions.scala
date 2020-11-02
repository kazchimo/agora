package infra.exchange.coincheck.impl

import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{sendR, SttpClient}
import sttp.ws.WebSocket
import zio.clock.Clock
import zio.console.putStrLn
import zio.stream.{Stream, ZStream}
import zio._

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  def useWS(que: Queue[String])(ws: WebSocket[RIO[Clock, *]]) = {
    val send    =
      ws.sendText("{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}")
    val receive = for {
      textEither <- ws.receiveTextFrame()
      text       <-
        ZIO
          .fromEither(textEither)
          .mapError(c =>
            InfraError(
              s"close websocket connection: statusCode=${c.statusCode.toString} reason=${c.reasonText}"
            )
          )
      _          <- que.offer(text.payload)
    } yield ()

    send *> ZIO.effectTotal(println("send websocket")) *> receive.forever
  }

  override def publicTransactions
    : ZIO[SttpClient with ZEnv, Throwable, ZStream[Any, Nothing, String]] =
    for {
      _   <- putStrLn("start public transactions")
      que <- Queue.unbounded[String]
      _   <- sendR[Long, Clock](
               basicRequest
                 .get(uri"${Endpoints.websocket}")
                 .response(asWebSocketAlways(useWS(que)))
             ).fork
      _   <- putStrLn("send complete")
    } yield Stream.fromQueue(que)
}
