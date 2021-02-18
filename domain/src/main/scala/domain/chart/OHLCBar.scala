package domain.chart

import domain.exchange.coincheck.CCPublicTransaction
import zio.Chunk

final case class OHLCBar(open: Double, high: Double, low: Double, close: Double)

object OHLCBar {
  def fromTransactions(ts: Chunk[CCPublicTransaction]): OHLCBar = {
    val rates = ts.map(_.rate.value.value)
    val max   = rates.max
    val min   = rates.min
    OHLCBar(ts.head.rate.value.value, max, min, ts.last.rate.value.value)
  }
}
