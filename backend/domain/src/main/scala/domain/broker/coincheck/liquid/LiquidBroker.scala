package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.FundingCurrency.Jpy
import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Price, Side}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.Pagination.Limit
import domain.exchange.liquid.Trade.Status.Open
import domain.exchange.liquid.Trade.TradingType.Cfd
import domain.exchange.liquid._
import domain.exchange.liquid.errors.UnprocessableEntity
import eu.timepit.refined.auto._
import lib.refined.PositiveInt
import lib.zio.{EStream, UpdatingRef}
import zio.duration._
import zio.logging.log
import zio.{RIO, Ref, Schedule, ZIO}

object LiquidBroker {

  def waitFilled(id: Id): ZIO[AllEnv, Throwable, Boolean] =
    waitFilledUntil(id, Duration.Infinity)

  /** @return whether the order is filled or not.
    */
  def waitFilledUntil(id: Id, d: Duration): ZIO[AllEnv, Throwable, Boolean] =
    for {
      stream: EStream[LiquidOrder] <- LiquidExchange.ordersStream
      filledRef                    <- Ref.make(false)
      _                            <- stream.interruptAfter(d).foreachWhile { o =>
                                        val notFilled = ZIO.succeed(
                                          o.notFilled
                                            || o.id != id
                                        )
                                        filledRef.set(true).unlessM(notFilled) *> notFilled
                                      }
      filled                       <- filledRef.get
      _                            <- log.debug(s"order filled: $filled")
    } yield filled

  def latestHeadPriceRef(side: Side): RIO[AllEnv, Ref[Option[Price]]] = for {
    stream: EStream[Seq[OrderOnBook]] <- LiquidExchange.orderBookStream(side)
    ref                               <- Ref.make[Option[Price]](None)
    _                                 <- stream.foreach { os =>
                                           ZIO
                                             .getOrFail(os.headOption)
                                             .flatMap(order => ref.set(Some(order.price)))
                                         }.fork
  } yield ref

  def openTradeCountRef(side: Trade.Side) = {
    val getTrades = LiquidExchange.getTrades(
      GetTradesParams(
        Some(btcJpyId),
        Some(Jpy),
        Some(Open),
        Some(side),
        Some(Cfd),
        Some(Limit(1000))
      )
    )

    for {
      trades <- getTrades
      ref    <- Ref.make(trades.size)
      fiber  <- getTrades
                  .flatMap(t =>
                    log.debug(s"Trade count is ${t.size}") *> ref.set(t.size)
                  ).repeat(Schedule.fixed(5.seconds))
                  .fork
    } yield UpdatingRef(ref, fiber)
  }

  def waitIfTradeCountIsOver(side: Trade.Side, threshold: PositiveInt) = for {
    ref        <- openTradeCountRef(side)
    countIsOver = ref.map(_ >= threshold.value)
    _          <- (countIsOver.get <* ZIO.sleep(1.seconds))
                    .repeatWhileEquals(true).whenM(countIsOver.get)
    _          <- ref.interruptUpdate
  } yield ()

  def createOrderWithWait[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S]
  ): RIO[AllEnv, LiquidOrder] = for {
    order <- LiquidExchange.createOrder(orderRequest)
    _     <- waitFilled(order.id)
  } yield order

  def retryNotEnoughBalanceOrder[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S],
    retryInterval: Duration
  ): RIO[AllEnv, LiquidOrder] = LiquidExchange
    .createOrder(orderRequest).retry(
      Schedule.fixed(retryInterval) && Schedule
        .recurWhile(_.isInstanceOf[UnprocessableEntity])
    )

  def timeoutedOrder[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S],
    d: Duration,
    retryNotEnoughBalance: Boolean = false,
    retryInterval: Duration = 10.seconds
  ): RIO[AllEnv, Boolean] = for {
    order  <- if (retryNotEnoughBalance)
                retryNotEnoughBalanceOrder(orderRequest, retryInterval)
              else LiquidExchange.createOrder(orderRequest)
    filled <- waitFilledUntil(order.id, d)
    _      <- LiquidExchange.cancelOrder(order.id).unless(filled)
  } yield filled
}
