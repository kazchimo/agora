package domain.exchange.bitflyer

sealed abstract class BFChildOrderType(val v: String)
case object BFLimitOrderType  extends BFChildOrderType("LIMIT")
case object BFMarketOrderType extends BFChildOrderType("MARKET")

sealed abstract class BFOrderSide(val v: String)
case object BFBuy  extends BFChildOrderType("BUY")
case object BFSell extends BFChildOrderType("SELL")

final case class BFChildOrder(tpe: BFChildOrderType, side: BFOrderSide)
bject BFChildOrder {}
