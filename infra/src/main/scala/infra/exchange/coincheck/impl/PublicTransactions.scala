package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCPublicTransaction
import domain.exchange.coincheck.CCPublicTransaction._
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import lib.error.InternalInfraError
import sttp.client3._
import sttp.client3.asynchttpclient.zio.{SttpClient, sendR}
import sttp.ws.WebSocket
import zio._
import zio.logging.{Logging, log}
import zio.stream.Stream

private[exchange] trait PublicTransactions { self: CoinCheckExchangeImpl =>
  private def useWS(
    que: Queue[CCPublicTransaction]
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
      body       <- PublicTransactions.textToModel(text.payload)
      _          <- que.offer(body)
    } yield ()

    send *> receive.forever
  }

  final override def publicTransactions: ZIO[
    SttpClient with ZEnv with Logging,
    Throwable,
    Stream[Nothing, CCPublicTransaction]
  ] = for {
    que <- Queue.unbounded[CCPublicTransaction]
    _   <- sendR[Unit, ZEnv with Logging](
             basicRequest
               .get(uri"${Endpoints.websocket}")
               .response(asWebSocketAlways(useWS(que)))
           ).fork
  } yield Stream.fromQueueWithShutdown(que).interruptWhen(que.awaitShutdown)
}

object PublicTransactions {
  def textToModel(text: String): ZIO[Any, Throwable, CCPublicTransaction] = {
    val regex = """[(\d),"(.*)","(\d*\.\d)","(\d*\.\d)","(.*)"]""".r

    def parse(s: String): IO[InternalInfraError, String] =
      ZIO.fromOption(Option(s)).orElseFail {
        InternalInfraError(
          s"Failed to parse body of Coincheck public transaction: $text"
        )
      }

    for {
      m           <- ZIO
                       .fromOption(regex.findFirstMatchIn(text)).orElseFail(
                         InternalInfraError(
                           s"Failed to parse body of Coincheck public transaction: $text"
                         )
                       )
      id          <- parse(m.group(1)).flatMap(CCPubTraId(_))
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
