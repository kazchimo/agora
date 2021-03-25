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
    latestBuyHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Buy)
    requestOrder           = for {
      _        <- LiquidBroker.waitIfTradeCountIsOver(Trade.Side.Long, tradeCount)
      buyPrice <- latestBuyHeadPriceRef.get.someOrFailException
      quote    <- TakeProfit(buyPrice.deepInnerV * 1.0005)
      stopLoss <- StopLoss(buyPrice.value * 0.995)
      request   = LiquidOrderRequest
                    .leveraged(btcJpyId, Buy, quantity, buyPrice, quote, stopLoss)
      _        <- LiquidBroker.timeoutedOrder(request, 10.seconds, true)
    } yield ()
    _                     <- requestOrder.whenM(latestBuyHeadPriceRef.get.map(_.nonEmpty)).forever
  } yield ()
}
