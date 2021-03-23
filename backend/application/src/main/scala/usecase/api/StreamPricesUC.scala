package usecase.api

import domain.AllEnv
import domain.exchange.Exchange
import domain.exchange.coincheck.{CCPublicTransaction, CoincheckExchange}
import domain.exchange.liquid.{LiquidExchange, LiquidExecution}
import lib.syntax.all._
import zio.stream.{Stream, UStream}
import zio.{Queue, ZIO}

final case class ExecutedOrder[Of <: Exchange](
  price: Double,
  time: Long,
  exchange: Of
)

object ExecutedOrder {
  def ofCoincheck(
    t: CCPublicTransaction
  ): ExecutedOrder[CoincheckExchange.type] = ExecutedOrder(
    t.rate.deepInnerV,
    System.currentTimeMillis,
    CoincheckExchange
  )

  def ofLiquid(e: LiquidExecution): ExecutedOrder[LiquidExchange.type] =
    ExecutedOrder(e.price.deepInnerV, e.createdAt.deepInnerV, LiquidExchange)
}

object StreamPricesUC {
  private type Str = Stream[Throwable, LiquidExecution]

  def getStream: ZIO[AllEnv, Throwable, UStream[ExecutedOrder[_]]] = for {
    coincheckStream   <- CoincheckExchange.publicTransactions
    liquidStream: Str <- LiquidExchange.executionStream
    queue             <- Queue.unbounded[ExecutedOrder[_ <: Exchange]]
    coincheckFiber    <-
      coincheckStream
        .foreach(t => queue.offer(ExecutedOrder.ofCoincheck(t))).fork
    liquidFiber       <-
      liquidStream.foreach(e => queue.offer(ExecutedOrder.ofLiquid(e))).fork
  } yield Stream.fromQueueWithShutdown(queue).interruptWhen(coincheckFiber.await.race(liquidFiber.await))
}
