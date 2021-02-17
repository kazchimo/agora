package usecase

import domain.exchange.coincheck.CoincheckExchange
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.{Has, Queue, Ref, ZEnv, ZIO}
import zio.stream.Stream
import zio.logging.{Logging, log}
import zio.duration._

final case class OHLCBar(open: Double, high: Double, low: Double, close: Double)

object TradeInDowMethodUC {
  def trade(aggCount: Int, continuous: Int): ZIO[
    CoincheckExchange with ZEnv with Logging with SttpClient,
    Throwable,
    Unit
  ] = for {
    _                  <- log.info("Buying in Dow method start...")
    transactionsStream <- CoincheckExchange.publicTransactions
    barsRef            <- Ref.make(Seq.empty[OHLCBar])
    onLongRef          <- Ref.make(false)
    tradeSummaryRef    <- Ref.make(0d)
    lastBuyRateRef     <- Ref.make(0d)
    _                  <- transactionsStream
                            .tap(a => log.trace(a.toString))
                            .grouped(aggCount).map { l =>
                              val rates = l.map(_.rate.value.value)
                              val max   = rates.max
                              val min   = rates.min
                              OHLCBar(l.head.rate.value.value, max, min, l.last.rate.value.value)
                            }.tap(a =>
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
                                _            <- if (bars.size >= continuous) {
                                                  val shouldBuy  =
                                                    bars.sortBy(_.high) == bars & bars.sortBy(_.low) == bars
                                                  val shouldSell = bars.sortBy(b => -b.high) == bars & bars
                                                    .sortBy(b => -b.low) == bars

                                                  if (shouldBuy & !onLong) log.info("Buy!") *> onLongRef
                                                    .set(true) *> lastBuyRateRef.set(bar.close)
                                                  else if (shouldSell & onLong) {
                                                    val profit  = bar.close - lastBuyRate
                                                    val summary = tradeSummary + profit
                                                    log.info("Sell!") *> onLongRef.set(false) *> log.info(
                                                      s"Profit: ${profit.toString} Summary: ${summary.toString}"
                                                    ) *> tradeSummaryRef.set(summary)
                                                  } else log.info("Do nothing")
                                                } else log.info("Do nothing")
                              } yield ()
                            }
  } yield ()
}
