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

  def trade(maxTradeCount: PositiveLong) = for {
    positionStateRef       <- Ref.make[PositionState](Neutral)
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    tradeCountRef          <- Ref.make(0)
    execute                 = for {
      state <- positionStateRef.get
      _     <- state match {
                 case Neutral                     => for {
                     price          <- latestBuyHeadPriceRef.get.someOrFailException
                     quote          <- price.zplus(Price.unsafeFrom(1d))
                     order          <-
                       LiquidExchange.createOrder(
                         LiquidOrderRequest.limitBuy(btcJpyId, quantity, quote)
                       )
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
                     _       <- tradeCountRef.update(_ + 1)
                     order   <- LiquidExchange.createOrder(
                                  LiquidOrderRequest
                                    .limitSell(btcJpyId, quantity, quote.max(plusOne))
                                )
                     _       <- LiquidBroker
                                  .waitFilledUntil(order.id, 1.minutes).zipLeft(
                                    tradeCountRef.update(_ - 1)
                                  ).unless(order.filled)
                     _       <- ZIO
                                  .sleep(1.second).whenM(
                                    tradeCountRef.get.map(_ <= maxTradeCount.value)
                                  ).repeatUntilM(_ =>
                                    tradeCountRef.get.map(_ <= maxTradeCount.value)
                                  )
                     _       <- positionStateRef.set(Neutral)
                   } yield ()
               }
    } yield ()
    _                      <- execute
                                .whenM(
                                  latestBuyHeadPriceRef.get
                                    .map(_.nonEmpty).zipWith(
                                      latestSellHeadPriceRef.get.map(_.nonEmpty)
                                    )(_ && _)
                                ).forever
  } yield ()
}
