package usecase.coincheck

import cats.Show
import cats.syntax.show._
import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.CCOrder.LimitOrder
import domain.exchange.coincheck.{CCOrderRequest, CoincheckExchange}
import domain.strategy.{DowMethod, Signal}
import lib.syntax.all._
import zio.Ref
import zio.logging.log

final case class TradingState(
  onLong: Boolean,
  tradeSummary: Double,
  lastBuyRate: Double
) {
  def toLongPosition: TradingState = this.copy(true)

  def toNeutralPosition: TradingState = this.copy(false)

  def buyAt(rate: Double): TradingState = this.copy(lastBuyRate = rate)

  def addSummary(d: Double): TradingState =
    this.copy(tradeSummary = d + this.tradeSummary)
}

object TradeInDowMethodUC {
  val jpy      = 50000
  val interval = 10

  implicit def orderRequestShow[T <: LimitOrder]
    : Show[CCOrderRequest[T]] = Show.show(o =>
    s"OrderRequest: rate=${o.limitRate.toString} amount=${o.limitAmount.toString}"
  )

  private def buy(signal: Signal, tradingStateRef: Ref[TradingState]) = for {
    request <- CCOrderRequest.limitBuy(signal.at, jpy / signal.at)
    _       <- log.info(s"Buy! ${request.show}")
    _       <- CoincheckBroker().priceAdjustingOrder(request, interval)
    _       <- tradingStateRef.update(_.toLongPosition.buyAt(signal.at))
  } yield ()

  private def sell(signal: Signal, tradingStateRef: Ref[TradingState]) = for {
    tradingState <- tradingStateRef.get
    request      <-
      CCOrderRequest.limitSell(signal.at, jpy / tradingState.lastBuyRate)
    _            <- log.info(s"Sell! ${request.show}")
    _            <- CoincheckBroker().priceAdjustingOrder(request, interval)
    tradingState <- tradingStateRef.get
    profit        =
      request.limitAmount.deepInnerV * (signal.at - tradingState.lastBuyRate)
    summary      <-
      tradingStateRef.updateAndGet(_.toNeutralPosition.addSummary(profit))
    _            <- log.info(
                      s"Profit: ${profit.toString} Total: ${summary.tradeSummary.toString}"
                    )
  } yield ()

  def trade(aggCount: Int, buyContinuous: Int, sellContinuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    tradingStateRef    <- Ref.make(TradingState(false, 0, 0))
    signalStream       <- DowMethod(aggCount, buyContinuous, sellContinuous)
                            .signal(transactionsStream)
    _                  <- signalStream.foreach { signal =>
                            for {
                              tradingState <- tradingStateRef.get
                              _            <- buy(signal, tradingStateRef)
                                                .when(signal.shouldBuy & !tradingState.onLong)
                              _            <- sell(signal, tradingStateRef)
                                                .when(signal.shouldSell & tradingState.onLong)
                            } yield ()
                          }
  } yield ()
}
