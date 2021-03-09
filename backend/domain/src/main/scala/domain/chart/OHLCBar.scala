package domain.chart

import domain.exchange.coincheck.CCPublicTransaction
import lib.syntax.all._
import zio.Chunk

final case class OHLCBar(
  open: Double,
  high: Double,
  low: Double,
  close: Double
) {
  def range: Double = high - low
}

object OHLCBar {
  def fromTransactions(ts: Chunk[CCPublicTransaction]): OHLCBar = {
    val rates = ts.map(_.rate.deepInnerV)
    val max   = rates.max
    val min   = rates.min
    OHLCBar(ts.head.rate.deepInnerV, max, min, ts.last.rate.deepInnerV)
  }
}
