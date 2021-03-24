package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{Id, OrderType, Price, Side}
import domain.exchange.liquid.{
  LiquidExchange,
  LiquidOrder,
  LiquidOrderRequest,
  OrderOnBook
}
import lib.zio.EStream
import zio.duration._
import zio.{Has, RIO, Ref, ZIO}

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
                                        val filled = ZIO.succeed(
                                          o.notFilled
                                            || o.id != id
                                        )
                                        filledRef.set(true).whenM(filled) *> filled
                                      }
      filled                       <- filledRef.get
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
  } yield filled
}
