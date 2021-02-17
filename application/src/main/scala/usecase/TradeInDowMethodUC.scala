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

object TradeInDowMethodUC {
  def trade(aggCount: Int, continuous: Int) = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    barsRef            <- Ref.make(Seq.empty[OHLCBar])
    onLongRef          <- Ref.make(false)
    tradeSummaryRef    <- Ref.make(0d)
    lastBuyRateRef     <- Ref.make(0d)
    _                  <- transactionsStream
                            .tap(a => log.trace(a.toString))
                            .grouped(aggCount).map(OHLCBar.fromTransactions).tap(a =>
                              log.info(s"High and Low per ${aggCount.toString}: ${a.toString}")
                            ).foreach { bar =>
                              for {
                                bars         <- barsRef.updateAndGet(old =>
                                                  if (old.size >= continuous) old.:+(bar).tail
                                                  else old.:+(bar)
                                                )
                                onLong       <- onLongRef.get
                                lastBuyRate  <- lastBuyRateRef.get
                                tradeSummary <- tradeSummaryRef.get
                                _            <- {
                                  val shouldBuy = bars.sortBy(_.high) == bars & bars
                                    .sortBy(_.low) == bars // high and low are increase
                                  val shouldSell = bars.sortBy(b => -b.high) == bars & bars
                                    .sortBy(b => -b.low) == bars // high and low are decrease

                                  val buyIf  = (log.info("Buy!") *> onLongRef.set(
                                    true
                                  ) *> lastBuyRateRef.set(bar.close)).when(shouldBuy & !onLong)
                                  val sellIf = {
                                    val profit  = bar.close - lastBuyRate
                                    val summary = tradeSummary + profit
                                    log.info("Sell!") *> onLongRef.set(false) *> log.info(
                                      s"Profit: ${profit.toString} Summary: ${summary.toString}"
                                    ) *> tradeSummaryRef.set(summary)
                                  }.when(shouldSell & onLong)

                                  buyIf *> sellIf
                                }.when(bars.size >= continuous)
                              } yield ()
                            }
  } yield ()
}
