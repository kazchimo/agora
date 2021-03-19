package usecase.liquid

import domain.exchange.liquid.LiquidOrder.{Price, Quantity}
import domain.exchange.liquid.{
  LiquidExchange,
  LiquidOrder,
  LiquidOrderRequest,
  OrderOnBook
}
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import lib.refined.PositiveDouble
import zio.{Ref, ZIO}
import zio.stream.Stream
import eu.timepit.refined.auto._

sealed private trait PositionState
private case object LongPosition extends PositionState
private case object Neutral      extends PositionState

object TradeHeadSpread {
  type Str = Stream[Throwable, Seq[OrderOnBook]]
  private val quantity: Quantity = Quantity.unsafeFrom(0.001)

  private def updatePrice(stream: Str, priceRef: Ref[Option[Price]]) =
    stream.foreach { orders =>
      for {
        order <- ZIO.getOrFail(orders.headOption)
        _     <- priceRef.set(Some(order.price))
      } yield ()
    }

  def trade = for {
    buyStream: Str         <- LiquidExchange.ordersStream(Buy)
    sellStream: Str        <- LiquidExchange.ordersStream(Sell)
    positionStateRef       <- Ref.make[PositionState](Neutral)
    latestBuyHeadPriceRef  <- Ref.make[Option[Price]](None)
    latestSellHeadPriceRef <- Ref.make[Option[Price]](None)
    _                      <- updatePrice(buyStream, latestBuyHeadPriceRef).fork
    _                      <- updatePrice(sellStream, latestSellHeadPriceRef).fork
    execute                 = for {
      state <- positionStateRef.get
      _     <- state match {
                 case Neutral      => for {
                     priceOpt <- latestBuyHeadPriceRef.get
                     price    <- ZIO.getOrFail(priceOpt)
                     orderReq  =
                       LiquidOrderRequest.limitBuy(btcJpyId, quantity, price)
                     order    <- LiquidExchange.createOrder(orderReq)
                     _        <- LiquidExchange
                                   .getOrder(order.id).repeatUntil(_.filled).unless(
                                     order.filled
                                   )
                     _        <- positionStateRef.set(LongPosition)
                   } yield ()
                 case LongPosition => for {
                     priceOpt <- latestSellHeadPriceRef.get
                     price    <- ZIO.getOrFail(priceOpt)
                     orderReq  =
                       LiquidOrderRequest.limitSell(btcJpyId, quantity, price)
                     order    <- LiquidExchange.createOrder(orderReq)
                     _        <- LiquidExchange
                                   .getOrder(order.id).repeatUntil(_.filled).unless(
                                     order.filled
                                   )
                     _        <- positionStateRef.set(Neutral)
                   } yield ()
               }
    } yield ()
    _                      <- execute
                                .whenM(
                                  ZIO.mapN(
                                    latestBuyHeadPriceRef.get.map(_.nonEmpty),
                                    latestSellHeadPriceRef.get.map(_.nonEmpty)
                                  )(_ && _)
                                ).forever
  } yield ()
}
