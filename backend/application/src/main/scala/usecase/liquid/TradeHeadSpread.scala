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
import zio.logging._

sealed private trait PositionState
private case class LongPosition(price: Price) extends PositionState
private case object Neutral                   extends PositionState

object TradeHeadSpread {
  private val quantity: Quantity = Quantity.unsafeFrom(0.0015)

  private def buy(price: Price) = LiquidExchange.createOrder(
    LiquidOrderRequest.limitBuy(btcJpyId, quantity, price)
  )

  private def sell(price: Price) = LiquidExchange.createOrder(
    LiquidOrderRequest.limitSell(btcJpyId, quantity, price)
  )

  private def neutralOpe(
    positionRef: Ref[PositionState],
    buyHeadRef: Ref[Option[Price]]
  ) = for {
    price          <- buyHeadRef.get.someOrFailException
    quote          <- price.zplus(Price.unsafeFrom(1d))
    order          <- buy(quote) <* log.info(s"Create buy order at ${quote.deepInnerV}")
    shouldRetryRef <- Ref.make(false)
    _              <- LiquidBroker
                        .waitFilled(order.id).unless(order.filled).zipRight(
                          log.info(s"Buy order settled at ${quote.deepInnerV}")
                        ).race(shouldRetryRef.set(true).delay(10.seconds))
    _              <- (LiquidExchange.cancelOrder(order.id) *> log.info(s"Reordering buy"))
                        .whenM(shouldRetryRef.get).fork
    _              <- positionRef.set(LongPosition(quote)).unlessM(shouldRetryRef.get)
  } yield ()

  private def checkTradeCount(countRef: Ref[Int], maxCount: Long) = {
    val info = countRef.get.flatMap(c =>
      log.info(
        s"Trade count are full. TradeCount=${c.toString}. Waiting settled..."
      )
    ) *> ZIO.sleep(5.seconds)

    val isOver = countRef.map(_ >= maxCount).get
    info.whenM(isOver).repeatWhileM(_ => isOver)
  }

  private def longOpe(
    tradeCountRef: Ref[Int],
    maxTradeCount: Long,
    positionRef: Ref[PositionState],
    sellHeadRef: Ref[Option[Price]],
    previousPrice: Price
  ) = for {
    price   <- sellHeadRef.get.someOrFailException
    quote   <- price.zminus(Price.unsafeFrom(1d))
    plusOne <- previousPrice.zplus(Price.unsafeFrom(1d))
    _       <- tradeCountRef.update(_ + 1)
    q        = quote.max(plusOne)
    order   <- sell(q) <* log.info(s"Created sell order at ${q.deepInnerV}")
    wait     =
      LiquidBroker.waitFilled(order.id).when(order.notFilled) *> tradeCountRef
        .update(_ - 1) *> log.info(s"Sell order settled at ${q.deepInnerV}")
    _       <- wait.race(wait.fork.delay(1.minutes))
    _       <- checkTradeCount(tradeCountRef, maxTradeCount)
    _       <- positionRef.set(Neutral)
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
    priceInitialized        = ZIO.mapN(
                                latestBuyHeadPriceRef.get.map(_.nonEmpty),
                                latestSellHeadPriceRef.get.map(_.nonEmpty)
                              )(_ && _)
    _                      <- execute.whenM(priceInitialized).forever
  } yield ()
}
