package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.{
  LeverageLevel,
  Quantity,
  StopLoss,
  TakeProfit
}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.errors.NotEnoughBalance
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest, Trade}
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import lib.instance.all._
import lib.refined.{PositiveDouble, PositiveInt, refineVZE}
import lib.syntax.all._
import zio.{Ref, Schedule, ZIO}
import zio.duration._
import zio.logging.log

import scala.math.Numeric._

object TradeHeadSpreadWithLeveraged {
  val quantity: Quantity  = Quantity.unsafeFrom(0.001)
  val one: PositiveDouble = 1d

  def trade(tradeCount: PositiveInt) = for {
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    countRef               <- LiquidBroker.openTradeCountRef(Trade.Side.Long)
    requestOrder            = for {
      c          <- countRef.get
      _          <- log.debug(c.toString)
      countIsOver = countRef.map(_ >= tradeCount.value)
      _          <- (countIsOver.get <* ZIO.sleep(1.seconds))
                      .repeatWhileEquals(true).whenM(countIsOver.get)
      buyPrice   <- latestBuyHeadPriceRef.get.someOrFailException
      quote      <- TakeProfit(buyPrice.deepInnerV * 1.0005)
      stopLoss   <- StopLoss(buyPrice.value * 0.995)
      request     = LiquidOrderRequest.leveraged(
                      btcJpyId,
                      Buy,
                      quantity,
                      buyPrice,
                      quote,
                      stopLoss,
                      LeverageLevel.unsafeApply(2L)
                    )
      _          <- LiquidBroker
                      .timeoutedOrder(request, 10.seconds).retry(
                        Schedule.fixed(10.seconds) && Schedule
                          .recurWhileEquals[Throwable](NotEnoughBalance)
                      )
    } yield ()
    _                      <- requestOrder
                                .whenM(
                                  latestBuyHeadPriceRef.get.map(_.nonEmpty) &&
                                    latestSellHeadPriceRef.get.map(_.nonEmpty)
                                ).forever
  } yield ()
}
