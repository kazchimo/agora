package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side._
import domain.exchange.liquid.LiquidOrder.{Quantity, StopLoss, TakeProfit}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import domain.exchange.liquid.{LiquidOrderRequest, Trade}
import eu.timepit.refined.auto._
import lib.refined.{PositiveDouble, PositiveInt}
import lib.syntax.all._
import zio.duration._

import scala.math.Numeric._

object TradeHeadSpreadWithLeveraged {
  val quantity: Quantity  = Quantity.unsafeFrom(0.001)
  val one: PositiveDouble = 1d

  def buy(tradeCount: PositiveInt) = for {
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

  def sell(tradeCount: PositiveInt) = for {
    latestBuyHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    requestOrder           = for {
      _         <- LiquidBroker.waitIfTradeCountIsOver(Trade.Side.Short, tradeCount)
      sellPrice <- latestBuyHeadPriceRef.get.someOrFailException
      quote     <- TakeProfit(sellPrice.deepInnerV * 0.9995)
      stopLoss  <- StopLoss(sellPrice.value * 1.005)
      request    = LiquidOrderRequest.leveraged(
                     btcJpyId,
                     Sell,
                     quantity,
                     sellPrice,
                     quote,
                     stopLoss
                   )
      _         <- LiquidBroker.timeoutedOrder(request, 10.seconds, true)
    } yield ()
    _                     <- requestOrder.whenM(latestBuyHeadPriceRef.get.map(_.nonEmpty)).forever
  } yield ()
}
