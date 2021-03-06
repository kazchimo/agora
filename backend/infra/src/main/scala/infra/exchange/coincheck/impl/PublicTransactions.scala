package infra.exchange.coincheck.impl

import domain.AllEnv
import domain.exchange.coincheck.CCPublicTransaction._
import domain.exchange.coincheck.{CCPublicTransaction, CoincheckExchange}
import infra.exchange.coincheck.Endpoints
import lib.error.InternalInfraError
import sttp.client3._
import sttp.client3.asynchttpclient.zio.sendR
import sttp.ws.{WebSocket, WebSocketClosed}
import zio._
import zio.logging.{Logging, log}
import zio.stream.Stream

private[exchange] trait PublicTransactions { self: CoincheckExchange.Service =>
  private def useWS(
    que: Queue[CCPublicTransaction]
  )(ws: WebSocket[RIO[AllEnv, *]]) = {
    val send    =
      ws.sendText("{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}")
    val receive = for {
      _    <- log.debug("Recceive websocket packet!")
      text <- ws.receiveTextFrame()
      body <- PublicTransactions
                .textToModel(text.payload).tapError(e =>
                  log.error(
                    s"Failed to parse text: ${e.toString} text=${text.payload}"
                  )
                )
      _    <- que.offer(body)
    } yield ()

    log.debug("websocket connection start") *> send *> receive.forever *> log
      .debug("websocket connection shutdowned")
  }

  final override def publicTransactions
    : ZIO[AllEnv, Throwable, Stream[Throwable, CCPublicTransaction]] = for {
    _     <- log.info("Querying coincheck public transactions...")
    que   <- Queue.unbounded[CCPublicTransaction]
    fiber <- sendR[Unit, AllEnv](
               basicRequest
                 .get(uri"${Endpoints.websocket}")
                 .response(asWebSocketAlways(useWS(que)))
             ).retryWhile(_.isInstanceOf[WebSocketClosed]).fork
  } yield Stream.fromQueueWithShutdown(que).interruptWhen(fiber.join)
}

object PublicTransactions {
  def textToModel(text: String): RIO[Logging, CCPublicTransaction] = {
    val regex = """\[(\d*),"(.*?)","(\d*\.\d*)","(\d*\.\d*)","(.*?)"\]""".r
    val error = InternalInfraError(
      s"Failed to parse body of Coincheck public transaction: $text"
    )

    def parse(s: => String) = ZIO
      .effect(s).foldM(
        e => ZIO.fail(error.copy(cause = Some(e))),
        {
          case null => ZIO.fail(error)
          case a    => ZIO.succeed(a)
        }
      )

    for {
      m           <- ZIO.fromOption(regex.findFirstMatchIn(text)).orElseFail(error)
      _           <- log.trace(s"Text: $text Matched object: ${m.toString()}")
      strId       <- parse(m.group(1))
      id          <- CCPubTraId(strId.toLong)
      pair        <- parse(m.group(2)).flatMap(CCPubTraPair(_))
      strRate     <- parse(m.group(3))
      rate        <- CCPubTraRate(strRate.toDouble)
      strQuantity <- parse(m.group(4))
      quantity    <- CCPubTraQuantity(strQuantity.toDouble)
      strSide     <- parse(m.group(5))
      side        <- ZIO.fromEither(CCPubTraSide.withNameEither(strSide))
    } yield CCPublicTransaction(id, pair, rate, quantity, side)
  }
}
