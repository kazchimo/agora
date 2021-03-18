package domain.strategy

import domain.chart.OHLCBar
import domain.exchange.coincheck.CCPublicTransaction
import zio._
import zio.logging.{Logging, log}
import zio.stream._
import lib.instance.all._
import lib.syntax.all._
import DowMethod._
import lib.refined.PositiveDouble

final case class DowMethod(
  aggCount: Int,
  buyContinuous: Int,
  sellContinuous: Int
) {
  def signal(
    tras: Stream[Throwable, PositiveDouble]
  ): URIO[Logging, UStream[Signal]] = for {
    barsForBuyRef  <- Ref.make(Chunk[OHLCBar]())
    barsForSellRef <- Ref.make(Chunk[OHLCBar]())
    signalQueue    <- Queue.unbounded[Signal]
    _              <- log.info("Finding signal by DowMethod...")
    _              <- tras
                        .grouped(aggCount).mapM(toBars).tap(a =>
                          log.info(s"OHLC per ${aggCount.toString}: ${a.toString}")
                        ).foreach { bar =>
                          for {
                            barsForBuy  <- updateBars(barsForBuyRef, buyContinuous, bar)
                            barsForSell <- updateBars(barsForSellRef, sellContinuous, bar)
                            barsAreFull  =
                              barsForBuy.size == buyContinuous & barsForSell.size == sellContinuous
                            _           <- signalQueue
                                             .offer(Buy(bar.close.value)).when(
                                               shouldBuy(barsForBuy) & barsAreFull
                                             )
                            _           <- signalQueue
                                             .offer(Sell(bar.close.value)).when(
                                               shouldSell(barsForSell) & barsAreFull
                                             )
                          } yield ()
                        }.fork
  } yield Stream.fromQueueWithShutdown(signalQueue).haltWhen(signalQueue.awaitShutdown)
}

object DowMethod {
  def toBars(c: Chunk[PositiveDouble]): Task[OHLCBar] = ZIO
    .getOrFail(NonEmptyChunk.fromChunk(c))
    .map(rates => OHLCBar.fromRates(rates))

  def updateBars[A](
    barsRef: Ref[Chunk[A]],
    continuous: Int,
    newBar: A
  ): UIO[Chunk[A]] = barsRef.updateAndGet(old =>
    if (old.size >= continuous) old.:+(newBar).tail
    else old.:+(newBar)
  )

  /** Return true if chart is increasing. */
  def shouldBuy(bars: Chunk[OHLCBar]): Boolean =
    bars.zipWithIndex.foldLeft(true) { case (should, (bar, idx)) =>
      if (idx == 0) true
      else should & bars(idx - 1).high < bar.high & bars(idx - 1).low < bar.low
    }

  /** Return true if chart is decreasing. */
  def shouldSell(bars: Seq[OHLCBar]): Boolean =
    bars.zipWithIndex.foldLeft(true) { case (should, (bar, idx)) =>
      if (idx == 0) true
      else should & bars(idx - 1).high > bar.high & bars(idx - 1).low > bar.low
    }
}
