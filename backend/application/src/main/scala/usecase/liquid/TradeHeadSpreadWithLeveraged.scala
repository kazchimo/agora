package usecase.liquid

import domain.broker.coincheck.liquid.LiquidBroker
import domain.exchange.liquid.LiquidOrder.Side.{Buy, Sell}
import domain.exchange.liquid.LiquidOrder.{Quantity, StopLoss, TakeProfit}
import domain.exchange.liquid.{LiquidExchange, LiquidOrderRequest, Trade}
import domain.exchange.liquid.LiquidProduct.btcJpyId
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import lib.instance.all._
import lib.refined.{PositiveDouble, refineVZE}
import lib.syntax.all._
import lib.zio.EStream
import zio.ZIO
import zio.duration._
import zio.logging.log
import zio.stream._

import scala.math.Numeric._

object TradeHeadSpreadWithLeveraged {
  val quantity: Quantity  = Quantity.unsafeFrom(0.001)
  val one: PositiveDouble = 1d

  sealed private trait ShouldRetry
  private object Should    extends ShouldRetry
  private object ShouldNot extends ShouldRetry

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
      order     <- LiquidExchange.createOrder(request)
      _         <- log.debug(order.toString)
      _         <- LiquidBroker
                     .waitFilled(order.id).as(ShouldNot).race(
                       ZIO.succeed(Should).delay(10.seconds)
                     ).tap {
                       case Should    => LiquidExchange.cancelOrder(order.id)
                       case ShouldNot => for {
                           str: EStream[Trade] <- LiquidExchange.tradesStream
                           _                   <- str.foreachWhile(t =>
                                                    ZIO.succeed(
                                                      t.id.value == order.id.value && t.closed
                                                    ) <* log.debug(t.toString)
                                                  )
                         } yield ()
                     }
    } yield ()
    _                      <- requestOrder
                                .whenM(
                                  latestBuyHeadPriceRef.get.map(_.nonEmpty) &&
                                    latestSellHeadPriceRef.get.map(_.nonEmpty)
                                ).forever
  } yield ()
}
