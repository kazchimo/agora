package infra.exchange.coincheck.impl

import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{send, SttpClient}
import sttp.ws.WebSocket
import zio.stream.{Stream, ZStream}
import zio.{Queue, RIO, ZIO}

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  private def useWS(que: Queue[String])(ws: WebSocket[RIO[Any, *]]) = for {
    textEither <- ws.receiveText()
    text       <-
      ZIO
        .fromEither(textEither)
        .mapError(c =>
          InfraError(
            s"close websocket connection: statusCode=${c.statusCode.toString} reason=${c.reasonText}"
          )
        )
    _          <- que.offer(text)
  } yield ()

  def publicTransactions
    : ZIO[SttpClient, Throwable, ZStream[Any, Nothing, String]] =
    for {
      que <- Queue.unbounded[String]
      _   <- send(
               basicRequest
                 .get(uri"${Endpoints.websocket}")
                 .body(Map("type" -> "subscribe", "channel" -> "btc_jpy-trades"))
                 .response(asWebSocketAlways(useWS(que)))
             )
    } yield Stream.fromQueue(que)
}
