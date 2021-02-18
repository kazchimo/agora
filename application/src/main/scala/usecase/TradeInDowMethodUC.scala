package usecase

import domain.exchange.coincheck.{CCPublicTransaction, CoincheckExchange}
import zio.logging.log
import zio.{Chunk, Ref}

final case class OHLCBar(open: Double, high: Double, low: Double, close: Double)

object OHLCBar {
  def fromTransactions(ts: Chunk[CCPublicTransaction]): OHLCBar = {
    val rates = ts.map(_.rate.value.value)
    val max   = rates.max
    val min   = rates.min
    OHLCBar(ts.head.rate.value.value, max, min, ts.last.rate.value.value)
  }
}

final case class TradingState(
  onLong: Boolean,
  tradeSummary: Double,
  lastBuyRate: Double
) {
  def toLongPosition: TradingState = this.copy(true)

  def toNeutralPosition: TradingState = this.copy(false)

  def buyAt(rate: Double): TradingState = this.copy(lastBuyRate = rate)

  def addSummary(d: Double): TradingState = this.copy(tradeSummary = d)
}

object TradeInDowMethodUC {
  private def shouldBuy(bars: Seq[OHLCBar]): Boolean =
    bars.sortBy(_.high) == bars & bars.sortBy(_.low) == bars

  private def shouldSell(bars: Seq[OHLCBar]): Boolean =
    bars.sortBy(b => -b.high) == bars & bars.sortBy(b => -b.low) == bars

  def trade(aggCount: Int, buyContinuous: Int, sellContinuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    barsForBuyRef      <- Ref.make(Seq.empty[OHLCBar])
    barsForSellRef     <- Ref.make(Seq.empty[OHLCBar])
    tradingStateRef    <- Ref.make(TradingState(false, 0, 0))
    _                  <- transactionsStream
                            .tap(a => log.trace(a.toString))
                            .grouped(aggCount).map(OHLCBar.fromTransactions).tap(a =>
                              log.info(s"High and Low per ${aggCount.toString}: ${a.toString}")
                            ).foreach { bar =>
                              for {
                                barsForBuy   <- barsForBuyRef.updateAndGet(old =>
                                                  if (old.size >= buyContinuous) old.:+(bar).tail
                                                  else old.:+(bar)
                                                )
                                barsForSell  <- barsForSellRef.updateAndGet(old =>
                                                  if (old.size >= sellContinuous) old.:+(bar).tail
                                                  else old.:+(bar)
                                                )
                                tradingState <- tradingStateRef.get
                                _            <- {
                                  val buyIf  = (log.info("Buy!") *> tradingStateRef.update(
                                    _.toLongPosition.buyAt(bar.close)
                                  )).when(shouldBuy(barsForBuy) & !tradingState.onLong)
                                  val sellIf = {
                                    val profit = bar.close - tradingState.lastBuyRate
                                    log.info("Sell!") *> tradingStateRef.update(
                                      _.toNeutralPosition.addSummary(profit)
                                    ) *> log.info(
                                      s"Profit: ${profit.toString} Summary: ${(tradingState.tradeSummary + profit).toString}"
                                    )
                                  }.when(shouldSell(barsForSell) & tradingState.onLong)

                                  buyIf *> sellIf
                                }.when(
                                  barsForBuy.size >= buyContinuous & barsForSell.size >= sellContinuous
                                )
                              } yield ()
                            }
  } yield ()
}
