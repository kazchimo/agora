package domain.exchange.bitflyer

import domain.exchange.bitflyer.BFChildOrder.BFOrderPrice
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory

sealed abstract class BFChildOrderType(val v: String)
case object BFLimitOrderType  extends BFChildOrderType("LIMIT")
case object BFMarketOrderType extends BFChildOrderType("MARKET")

sealed abstract class BFOrderSide(val v: String)
case object BFBuy  extends BFChildOrderType("BUY")
case object BFSell extends BFChildOrderType("SELL")

final case class BFChildOrder(tpe: BFChildOrderType, side: BFOrderSide, price: BFOrderPrice)
object BFChildOrder {
  @newtype case class BFOrderPrice(v: Long Refined Positive)
  object BFOrderPrice extends VOFactory[Long, Positive] {
    override type VO = BFOrderPrice
  }
}
