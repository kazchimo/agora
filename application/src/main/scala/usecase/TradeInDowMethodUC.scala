package usecase

import domain.exchange.coincheck.CoincheckExchange
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
  def trade(aggCount: Int, buyContinuous: Int, sellContinuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    tradingStateRef    <- Ref.make(TradingState(false, 0, 0))
    signalStream       <- DowMethod(aggCount, buyContinuous, sellContinuous)
                            .signal(transactionsStream)
    _                  <- signalStream.foreach { signal =>
                            for {
                              tradingState <- tradingStateRef.get
                              _            <- (log.info("Buy!") *> tradingStateRef.update(
                                                _.toLongPosition.buyAt(signal.at)
                                              )).when(signal.shouldBuy & !tradingState.onLong)
                              _            <- {
                                for {
                                  _            <- log.info("Sell!")
                                  tradingState <- tradingStateRef.get
                                  profit        = signal.at - tradingState.lastBuyRate
                                  summary      <- tradingStateRef.updateAndGet(
                                                    _.toNeutralPosition.addSummary(profit)
                                                  )
                                  _            <-
                                    log.info(
                                      s"Profit: ${profit.toString} Summary: ${summary.tradeSummary.toString}"
                                    )
                                } yield ()
                              }.when(signal.shouldSell & tradingState.onLong)
                            } yield ()
                          }
  } yield ()
}
