package domain.chart

import domain.chart.OHLCBar._
import domain.exchange.coincheck.CCPublicTransaction
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.instance.all._
import lib.refined.PositiveDouble
import lib.syntax.all._
import zio.{Chunk, NonEmptyChunk, ZIO}

final case class OHLCBar(open: Open, high: High, low: Low, close: Close) {
  def range: Double = high.deepInnerV - low.deepInnerV
}

object OHLCBar {
  @newtype case class Open(value: PositiveDouble)
  object Open extends VOFactory

  @newtype case class High(value: PositiveDouble)
  object High extends VOFactory

  @newtype case class Low(value: PositiveDouble)
  object Low extends VOFactory

  @newtype case class Close(value: PositiveDouble)
  object Close extends VOFactory

  def unsafeApply(
    open: Double,
    high: Double,
    low: Double,
    close: Double
  ): OHLCBar = OHLCBar(
    Open.unsafeFrom(open),
    High.unsafeFrom(high),
    Low.unsafeFrom(low),
    Close.unsafeFrom((close))
  )

  def fromRates(rates: NonEmptyChunk[PositiveDouble]): OHLCBar = OHLCBar(
    Open(rates.head),
    High(rates.max),
    Low(rates.min),
    Close(rates.last)
  )
}
