package domain.chart

import domain.chart.OHLCBar._
import domain.exchange.coincheck.CCPublicTransaction
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.instance.all._
import lib.refined.PositiveDouble
import lib.syntax.all._
import zio.Chunk

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

  def fromTransactions(ts: Chunk[CCPublicTransaction]): OHLCBar = {
    val rates = ts.map(_.rate.value)
    OHLCBar(
      Open(ts.head.rate.value),
      High(rates.max),
      Low(rates.min),
      Close(ts.last.rate.value)
    )
  }
}
