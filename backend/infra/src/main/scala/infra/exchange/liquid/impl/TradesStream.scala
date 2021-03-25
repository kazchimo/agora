package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.FundingCurrency.Jpy
import domain.exchange.liquid.{LiquidExchange, Trade}
import infra.exchange.liquid.response.TradeResponse
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.refined._
import sttp.ws.WebSocket
import zio.logging.log
import zio.stream.Stream
import zio.{Queue, RIO, ZIO, stream}

private[liquid] trait TradesStream extends WebSocketHandler {
  self: LiquidExchange.Service =>

  private def useWS(queue: Queue[Trade])(ws: WebSocket[RIO[AllEnv, *]]) =
    handleAuthMessage(ws, f"user_account_${Jpy.entryName}_trades", "updated") {
      d =>
        for {
          res   <- ZIO.fromEither(decode[TradeResponse](d.data))
          trade <- res.toTrade
          _     <- log.trace(trade.toString)
          _     <- queue.offer(trade)
        } yield ()
    }.forever

  override def tradesStream: RIO[AllEnv, stream.Stream[Throwable, Trade]] =
    for {
      queue <- Queue.unbounded[Trade]
      fiber <- sendWS(useWS(queue)).fork
    } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}
