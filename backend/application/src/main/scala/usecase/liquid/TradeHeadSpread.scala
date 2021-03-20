package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.{Price, Quantity}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest}
import eu.timepit.refined.auto._
import lib.instance.all._
import lib.syntax.all._
import zio.duration._
import zio.logging._
import zio.{Ref, ZIO}

sealed private trait PositionState
private case class LongPosition(price: Price) extends PositionState
private case object Neutral                   extends PositionState

object TradeHeadSpread {
  private val quantity: Quantity = Quantity.unsafeFrom(0.001)

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

  private def longOpe(
    positionRef: Ref[PositionState],
    sellHeadRef: Ref[Option[Price]],
    previousPrice: Price
  ) = for {
    price   <- sellHeadRef.get.someOrFailException
    quote   <- price.zminus(Price.unsafeFrom(1d))
    plusOne <- previousPrice.zplus(Price.unsafeFrom(1d))
    q        = quote.max(plusOne)
    order   <- sell(q) <* log.info(s"Created sell order at ${q.deepInnerV}")
    _       <- LiquidBroker.waitFilled(order.id).when(order.notFilled) *> log.info(
                 s"Sell order settled at ${q.deepInnerV}"
               )
    _       <- positionRef.set(Neutral)
  } yield ()

  def trade = for {
    positionStateRef       <- Ref.make[PositionState](Neutral)
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    execute                 = for {
      state <- positionStateRef.get
      _     <- state match {
                 case Neutral                     => neutralOpe(positionStateRef, latestBuyHeadPriceRef)
                 case LongPosition(previousPrice) =>
                   longOpe(positionStateRef, latestSellHeadPriceRef, previousPrice)
               }
    } yield ()
    priceInitialized        = ZIO.mapN(
                                latestBuyHeadPriceRef.get.map(_.nonEmpty),
                                latestSellHeadPriceRef.get.map(_.nonEmpty)
                              )(_ && _)
    _                      <- execute.whenM(priceInitialized).forever
  } yield ()
}
