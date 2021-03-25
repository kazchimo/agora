package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.FundingCurrency.Jpy
import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Price, Side}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.Trade.Status.Open
import domain.exchange.liquid.Trade.TradingType.Cfd
import domain.exchange.liquid.{
  GetTradesParams,
  LiquidExchange,
  LiquidOrder,
  LiquidOrderRequest,
  OrderOnBook,
  Trade
}
import lib.zio.{EStream, UReadOnlyRef}
import zio.duration._
import zio.logging.log
import zio.{Has, RIO, Ref, ZIO, ZRef}

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

  def openTradeCountRef(side: Trade.Side): RIO[AllEnv, UReadOnlyRef[Int]] =
    for {
      trades              <- LiquidExchange.getTrades(
                               GetTradesParams(
                                 Some(btcJpyId),
                                 Some(Jpy),
                                 Some(Open),
                                 Some(side),
                                 Some(Cfd)
                               )
                             )
      ref                 <- Ref.make(trades.size)
      str: EStream[Trade] <- LiquidExchange.tradesStream
      _                   <- str.foreach { t =>
                               log.debug(t.toString) *> (if (t.closed) ref.update(_ - 1)
                                                         else ref.update(_ + 1))
                             }.fork
    } yield ref.readOnly

  def createOrderWithWait[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S]
  ): RIO[AllEnv, LiquidOrder] = for {
    order <- LiquidExchange.createOrder(orderRequest)
    _     <- waitFilled(order.id)
  } yield order

  def timeoutedOrder[O <: OrderType, S <: Side](
    orderRequest: LiquidOrderRequest[O, S],
    d: Duration
  ): RIO[AllEnv, Boolean] = for {
    order  <- LiquidExchange.createOrder(orderRequest)
    filled <- waitFilledUntil(order.id, d)
    _      <- LiquidExchange.cancelOrder(order.id).unless(filled)
  } yield filled
}
