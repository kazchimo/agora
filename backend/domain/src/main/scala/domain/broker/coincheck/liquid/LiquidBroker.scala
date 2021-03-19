package domain.broker.coincheck.liquid

import domain.AllEnv
import domain.exchange.liquid.LiquidOrder.{Id, Price, Side}
import domain.exchange.liquid.{LiquidExchange, LiquidOrder, OrderOnBook}
import zio.{Has, RIO, Ref, ZIO}
import zio.duration._
import zio.stream.Stream

object LiquidBroker {
  def waitFilled(id: Id): ZIO[AllEnv, Throwable, LiquidOrder] = LiquidExchange
    .getOrder(id).repeatUntilM(o =>
      if (o.filled) ZIO.succeed(true) else ZIO.sleep(1.seconds).as(false)
    )

  def latestHeadPriceRef(side: Side): RIO[AllEnv, Ref[Option[Price]]] = for {
    stream: Stream[Throwable, Seq[OrderOnBook]] <-
      LiquidExchange.ordersStream(side)
    ref                                         <- Ref.make[Option[Price]](None)
    _                                           <- stream.foreach { os =>
                                                     ZIO
                                                       .getOrFail(os.headOption)
                                                       .flatMap(order => ref.set(Some(order.price)))
                                                   }.fork
  } yield ref
}