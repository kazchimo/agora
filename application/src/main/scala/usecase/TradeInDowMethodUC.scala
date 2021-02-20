package usecase

import domain.broker.coincheck.CoincheckBroker
import domain.exchange.coincheck.{
  CCLimitBuyRequest,
  CCLimitSellRequest,
  CoincheckExchange
}
import domain.strategy.DowMethod
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

  def trade(aggCount: Int, buyContinuous: Int, sellContinuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    tradingStateRef    <- Ref.make(TradingState(false, 0, 0))
    signalStream       <- DowMethod(aggCount, buyContinuous, sellContinuous)
                            .signal(transactionsStream)
    broker              = CoincheckBroker(transactionsStream)
    _                  <- signalStream.foreach { signal =>
                            for {
                              tradingState <- tradingStateRef.get
                              _            <- {
                                for {
                                  _       <- log.info("Buy!")
                                  request <-
                                    CCLimitBuyRequest.fromRaw(signal.at, jpy / signal.at)
                                  _       <- broker.priceAdjustingOrder(request, interval)
                                  _       <- tradingStateRef.update(_.toLongPosition.buyAt(signal.at))
                                } yield ()
                              }.when(signal.shouldBuy & !tradingState.onLong)
                              _            <- {
                                for {
                                  _            <- log.info("Sell!")
                                  request      <- CCLimitSellRequest.fromRaw(
                                                    signal.at,
                                                    jpy / tradingState.lastBuyRate
                                                  )
                                  _            <- broker.priceAdjustingOrder(request, interval)
                                  tradingState <- tradingStateRef.get
                                  profit        =
                                    request.amount.value.value * (signal.at - tradingState.lastBuyRate)
                                  summary      <- tradingStateRef.updateAndGet(
                                                    _.toNeutralPosition.addSummary(profit)
                                                  )
                                  _            <-
                                    log.info(
                                      s"Profit: ${profit.toString} Total: ${summary.tradeSummary.toString}"
                                    )
                                } yield ()
                              }.when(signal.shouldSell & tradingState.onLong)
                            } yield ()
                          }
  } yield ()
}
