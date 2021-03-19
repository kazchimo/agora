package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
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
import zio.duration._
import lib.syntax.all._
import lib.instance.all._

sealed private trait PositionState
private case class LongPosition(price: Price) extends PositionState
private case object Neutral                   extends PositionState

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
                 case Neutral                     => for {
                     price          <- latestBuyHeadPriceRef.get.someOrFailException
                     quote          <- price.zplus(Price.unsafeFrom(1d))
                     orderReq        =
                       LiquidOrderRequest.limitBuy(btcJpyId, quantity, quote)
                     order          <- LiquidExchange.createOrder(orderReq)
                     shouldRetryRef <- Ref.make(false)
                     _              <- LiquidBroker
                                         .waitFilled(order.id).unless(order.filled).race(
                                           shouldRetryRef.set(true).delay(10.seconds)
                                         )
                     _              <- LiquidExchange
                                         .cancelOrder(order.id).whenM(shouldRetryRef.get).fork
                     _              <- positionStateRef
                                         .set(LongPosition(quote)).unlessM(shouldRetryRef.get)
                   } yield ()
                 case LongPosition(previousPrice) => for {
                     price   <- latestSellHeadPriceRef.get.someOrFailException
                     quote   <- price.zminus(Price.unsafeFrom(1d))
                     plusOne <- previousPrice.zplus(Price.unsafeFrom(1d))
                     orderReq = LiquidOrderRequest
                                  .limitSell(btcJpyId, quantity, quote.max(plusOne))
                     order   <- LiquidExchange.createOrder(orderReq)
                     _       <- LiquidBroker.waitFilled(order.id).unless(order.filled)
                     _       <- positionStateRef.set(Neutral)
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
