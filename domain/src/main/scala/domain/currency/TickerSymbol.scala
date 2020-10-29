package domain.currency

import lib.factory.SumVOFactory

sealed trait TickerSymbol extends Serializable with Product {
  val value: String
}

object TickerSymbol       extends SumVOFactory              {
  override type VO = TickerSymbol
  override val sums: Seq[TickerSymbol]               = Seq(BitCoin, Jpy)
  override def extractValue(v: TickerSymbol): String = v.value
}

case object BitCoin extends TickerSymbol { override val value: String = "btc" }
case object Jpy     extends TickerSymbol { override val value: String = "jpy" }
