package infra.exchange.coincheck.impl

import infra.exchange.coincheck.Endpoints
import sttp.capabilities.zio.ZioStreams
import sttp.client3.asynchttpclient.zio.{sendR, SttpClient}
import sttp.client3._
import sttp.ws.WebSocket
import zio.{Task, ZIO}

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  private def useWebsocket(ws: WebSocket[Task[_]]) = ws.receiveText()

  def publicTransactions: ZIO[SttpClient, Throwable, Response[Nothing]] =
    sendR(
      basicRequest
        .get(uri"${Endpoints.websocket}")
        .body(Map("type" -> "subscribe", "channel" -> "btc_jpy-trades"))
        .response(asWebSocketStreamAlways(ZioStreams))
    )
}
