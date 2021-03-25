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
import domain.exchange.liquid.{LiquidOrderRequest, Trade}
import eu.timepit.refined.auto._
import lib.refined.{PositiveDouble, PositiveInt}
import lib.syntax.all._
import zio.ZIO
import zio.duration._

import scala.math.Numeric._

object TradeHeadSpreadWithLeveraged {
  val quantity: Quantity  = Quantity.unsafeFrom(0.001)
  val one: PositiveDouble = 1d

  def trade(tradeCount: PositiveInt) = for {
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    countRef               <- LiquidBroker.openTradeCountRef(Trade.Side.Long)
    countIsOver             = countRef.map(_ >= tradeCount.value)
    requestOrder            = for {
      _        <- (countIsOver.get <* ZIO.sleep(1.seconds))
                    .repeatWhileEquals(true).whenM(countIsOver.get)
      buyPrice <- latestBuyHeadPriceRef.get.someOrFailException
      quote    <- TakeProfit(buyPrice.deepInnerV * 1.0005)
      stopLoss <- StopLoss(buyPrice.value * 0.995)
      request   = LiquidOrderRequest.leveraged(
                    btcJpyId,
                    Buy,
                    quantity,
                    buyPrice,
                    quote,
                    stopLoss,
                    LeverageLevel.unsafeApply(2L)
                  )
      _        <- LiquidBroker.timeoutedOrder(request, 10.seconds, true)
    } yield ()
    _                      <- requestOrder
                                .whenM(
                                  latestBuyHeadPriceRef.get.map(_.nonEmpty) &&
                                    latestSellHeadPriceRef.get.map(_.nonEmpty)
                                ).forever
  } yield ()
}
