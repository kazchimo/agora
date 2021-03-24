package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.{Quantity, StopLoss, TakeProfit}
import domain.exchange.liquid.LiquidOrderRequest
import domain.exchange.liquid.LiquidProduct.btcJpyId
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import lib.instance.all._
import lib.refined.{PositiveDouble, refineVZE}
import lib.syntax.all._

import scala.math.Numeric._

object TradeHeadSpreadWithLeveraged {
  val quantity: Quantity  = Quantity.unsafeFrom(0.001)
  val one: PositiveDouble = 1d

  def trade = for {
    latestBuyHeadPriceRef  <- LiquidBroker.latestHeadPriceRef(Buy)
    latestSellHeadPriceRef <- LiquidBroker.latestHeadPriceRef(Sell)
    requestOrder            = for {
      buyPrice  <- latestBuyHeadPriceRef.get.someOrFailException
      sellPrice <- latestSellHeadPriceRef.get.someOrFailException
      plusOne   <- refineVZE[Positive, Double](buyPrice.deepInnerV + 1)
      quote      = Ordering[PositiveDouble].max(sellPrice.value, plusOne)
      stopLoss  <- StopLoss(buyPrice.value * 0.95)
      request    = LiquidOrderRequest.leveraged(
                     btcJpyId,
                     Buy,
                     quantity,
                     buyPrice,
                     TakeProfit(quote),
                     stopLoss
                   )
      _         <- LiquidBroker.createOrderWithWait(request)
    } yield ()
    _                      <- requestOrder.forever
  } yield ()
}
