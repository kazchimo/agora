package infra.exchange.liquid.impl

import domain.AllEnv
import domain.exchange.liquid.{LiquidExchange, LiquidOrder}
import infra.exchange.liquid.response.OrderResponse
import sttp.ws.WebSocket
import zio.{Queue, RIO, ZIO, stream}
import zio.stream.Stream
import io.circe.parser.decode
import io.circe.refined._
import io.circe.generic.auto._

private[liquid] trait OrdersStream extends WebSocketHandler {
  self: LiquidExchange.Service =>
  private def useWS(queue: Queue[LiquidOrder])(ws: WebSocket[RIO[AllEnv, *]]) =
    handleAuthMessage(ws, "user_account_jpy_orders", "updated") { d =>
      for {
        res   <- ZIO.fromEither(decode[OrderResponse](d.data))
        order <- res.toOrder
        _     <- queue.offer(order)
      } yield ()
    }

  override def ordersStream
    : RIO[AllEnv, stream.Stream[Throwable, LiquidOrder]] = for {
    queue <- Queue.unbounded[LiquidOrder]
    fiber <- sendWS(useWS(queue)).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(fiber.join)
}
