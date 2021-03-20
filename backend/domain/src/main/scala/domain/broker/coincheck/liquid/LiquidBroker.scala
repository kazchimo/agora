package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{Id, Price, Side}
import domain.exchange.liquid.{LiquidExchange, LiquidOrder, OrderOnBook}
import lib.zio.EStream
import zio.duration._
import zio.stream.Stream
import zio.{RIO, Ref, ZIO}

object LiquidBroker {

  def waitFilled(id: Id): ZIO[AllEnv, Throwable, Unit] =
    waitFilledUntil(id, Duration.Infinity)

  def waitFilledUntil(id: Id, d: Duration): ZIO[AllEnv, Throwable, Unit] = for {
    stream: EStream[LiquidOrder] <- LiquidExchange.ordersStream
    _                            <- stream
                                      .interruptAfter(d).foreachWhile(o =>
                                        ZIO.succeed(
                                          o.notFilled
                                            || o.id != id
                                        )
                                      )
  } yield ()

  def latestHeadPriceRef(side: Side): RIO[AllEnv, Ref[Option[Price]]] = for {
    stream: EStream[Seq[OrderOnBook]] <- LiquidExchange.orderBookStream(side)
    ref                               <- Ref.make[Option[Price]](None)
    _                                 <- stream.foreach { os =>
                                           ZIO
                                             .getOrFail(os.headOption)
                                             .flatMap(order => ref.set(Some(order.price)))
                                         }.fork
  } yield ref
}
