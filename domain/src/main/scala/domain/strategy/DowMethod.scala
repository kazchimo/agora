package domain.strategy
import domain.chart.OHLCBar
import domain.exchange.coincheck.CCPublicTransaction
import zio.logging.log
import zio.stream._
import zio.{Chunk, Queue, Ref}

final case class DowMethod(
  aggCount: Int,
  buyContinuous: Int,
  sellContinuous: Int
) {
  private def updateBars[A](
    barsRef: Ref[Chunk[A]],
    continuous: Int,
    newBar: A
  ) = barsRef.updateAndGet(old =>
    if (old.size >= continuous) old.:+(newBar).tail
    else old.:+(newBar)
  )

  private def shouldBuy(bars: Chunk[OHLCBar]): Boolean =
    bars.sortBy(_.high) == bars & bars.sortBy(_.low) == bars

  private def shouldSell(bars: Seq[OHLCBar]): Boolean =
    bars.sortBy(b => -b.high) == bars & bars.sortBy(b => -b.low) == bars

  def signal(tras: UStream[CCPublicTransaction]) = for {
    barsForBuyRef  <- Ref.make(Chunk[OHLCBar]())
    barsForSellRef <- Ref.make(Chunk[OHLCBar]())
    signalQueue    <- Queue.unbounded[Signal]
    _              <- log.info("Finding signal by DowMethod...")
    _              <- tras
                        .grouped(aggCount).map(OHLCBar.fromTransactions).tap(a =>
                          log.info(s"OHLC per ${aggCount.toString}: ${a.toString}")
                        ).foreach { bar =>
                          for {
                            barsForBuy  <- updateBars(barsForBuyRef, buyContinuous, bar)
                            barsForSell <- updateBars(barsForSellRef, sellContinuous, bar)
                            barsAreFull  =
                              barsForBuy.size == buyContinuous & barsForSell.size == sellContinuous
                            _           <- signalQueue
                                             .offer(Buy(bar.close)).when(
                                               shouldBuy(barsForBuy) & barsAreFull
                                             )
                            _           <- signalQueue
                                             .offer(Sell(bar.close)).when(
                                               shouldSell(barsForSell) & barsAreFull
                                             )
                          } yield ()
                        }.fork
  } yield Stream.fromQueueWithShutdown(signalQueue).interruptWhen(signalQueue.awaitShutdown)
}
