package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.{Price, Quantity}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest, OrderOnBook}
import eu.timepit.refined.auto._
import lib.instance.all._
import lib.refined.PositiveLong
import lib.syntax.all._
import zio.duration._
import zio.stream.Stream
import zio.{Ref, ZIO}

sealed private trait PositionState
private case class LongPosition(price: Price) extends PositionState
private case object Neutral                   extends PositionState

object TradeHeadSpread {
  type Str = Stream[Throwable, Seq[OrderOnBook]]
  private val quantity: Quantity = Quantity.unsafeFrom(0.0015)

  private def neutralOpe(
    positionRef: Ref[PositionState],
    buyHeadRef: Ref[Option[Price]]
  ) = for {
    price          <- buyHeadRef.get.someOrFailException
    quote          <- price.zplus(Price.unsafeFrom(1d))
    order          <- LiquidExchange.createOrder(
                        LiquidOrderRequest.limitBuy(btcJpyId, quantity, quote)
                      )
    shouldRetryRef <- Ref.make(false)
    _              <- LiquidBroker
                        .waitFilled(order.id).unless(order.filled).race(
                          shouldRetryRef.set(true).delay(10.seconds)
                        )
    _              <- LiquidExchange.cancelOrder(order.id).whenM(shouldRetryRef.get).fork
    _              <- positionRef.set(LongPosition(quote)).unlessM(shouldRetryRef.get)
  } yield ()

  private def longOpe(
    tradeCountRef: Ref[Int],
    maxTradeCount: Long,
    positionRef: Ref[PositionState],
    sellHeadRef: Ref[Option[Price]],
    previousPrice: Price
  ) = for {
    price         <- sellHeadRef.get.someOrFailException
    quote         <- price.zminus(Price.unsafeFrom(1d))
    plusOne       <- previousPrice.zplus(Price.unsafeFrom(1d))
    _             <- tradeCountRef.update(_ + 1)
    order         <-
      LiquidExchange.createOrder(
        LiquidOrderRequest.limitSell(btcJpyId, quantity, quote.max(plusOne))
      )
    shouldForkRef <- Ref.make(false)
    decTradeCount  = tradeCountRef.update(_ - 1)
    wait           =
      LiquidBroker.waitFilled(order.id).unless(order.filled) *> decTradeCount
    _             <- wait.race(shouldForkRef.set(true).delay(1.minutes))
    _             <- wait.fork.whenM(shouldForkRef.get)
    countLessRef   = tradeCountRef.get.map(_ <= maxTradeCount)
    _             <- ZIO.sleep(1.second).whenM(countLessRef).repeatUntilM(_ => countLessRef)
    _             <- positionRef.set(Neutral)
  } yield ()

  def trade(maxTradeCount: PositiveLong) = for {
    positionStateRef       <- Ref.make[PositionState](Neutral)
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    tradeCountRef          <- Ref.make(0)
    execute                 = for {
      state <- positionStateRef.get
      _     <- state match {
                 case Neutral                     => neutralOpe(positionStateRef, latestBuyHeadPriceRef)
                 case LongPosition(previousPrice) => longOpe(
                     tradeCountRef,
                     maxTradeCount.value,
                     positionStateRef,
                     latestSellHeadPriceRef,
                     previousPrice
                   )
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
