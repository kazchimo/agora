package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.LiquidCurrencyPairCode.BtcJpy
import domain.exchange.liquid.{LiquidExchange, LiquidExecution}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.refined._
import lib.error.ClientDomainError
import lib.refined.{NonNegativeDouble, NonNegativeLong}
import sttp.ws.WebSocket
import zio._
import zio.stream._

private[liquid] case class ExecutionResponse(
  id: NonNegativeLong,
  created_at: NonNegativeLong,
  price: NonNegativeDouble,
  quantity: NonNegativeDouble,
  taker_side: String
) {
  def toLiquidExecution: IO[ClientDomainError, LiquidExecution] =
    LiquidExecution.TakerSide.withNameZio(taker_side).map { side =>
      LiquidExecution(
        LiquidExecution.Id(id),
        LiquidExecution.Quantity(quantity),
        LiquidExecution.Price(price),
        side,
        LiquidExecution.CreatedAt(created_at)
      )
    }
}

private[liquid] trait ExecutionStream extends WebSocketHandler {
  self: LiquidExchange.Service =>
  private def useWS(
    queue: Queue[LiquidExecution]
  )(ws: WebSocket[RIO[AllEnv, *]]) =
    handleMessage(ws, s"executions_cash_${BtcJpy.entryName}", "created") { d =>
      for {
        res <- ZIO.fromEither(decode[ExecutionResponse](d.data))
        e   <- res.toLiquidExecution
        _   <- queue.offer(e)
      } yield ()
    }.forever

  override def executionStream
    : ZIO[AllEnv, Throwable, stream.Stream[Throwable, LiquidExecution]] = for {
    queue <- Queue.unbounded[LiquidExecution]
    fiber <- sendWS(useWS(queue)).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}
