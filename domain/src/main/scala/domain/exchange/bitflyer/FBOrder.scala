package domain.exchange.bitflyer

import domain.exchange.bitflyer.BFChildOrder.{
  BFOrderMinuteToExpire,
  BFOrderPrice,
  BFOrderSize
}
import domain.lib.VOFactory
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype

// https://lightning.bitflyer.com/docs?lang=ja#%E6%96%B0%E8%A6%8F%E6%B3%A8%E6%96%87%E3%82%92%E5%87%BA%E3%81%99
final case class BFChildOrder(
  tpe: BFChildOrderType,
  side: BFOrderSide,
  price: BFOrderPrice,
  size: BFOrderSize,
  timeInForce: BFQuantityConditionsEnforcement,
  productCode: BFProductCode,
  minuteToExpire: Option[BFOrderMinuteToExpire] = None
)

object BFChildOrder {
  @newtype case class BFOrderPrice(v: Long Refined Positive)
  object BFOrderPrice extends VOFactory[Long, Positive] {
    override type VO = BFOrderPrice
  }

  @newtype case class BFOrderSize(v: Double Refined Positive)
  object BFOrderSize extends VOFactory[Double, Positive] {
    override type VO = BFOrderSize
  }

  @newtype case class BFOrderMinuteToExpire(v: Long Refined Positive)
  object BFOrderMinuteToExpire extends VOFactory[Long, Positive] {
    override type VO = BFOrderMinuteToExpire
  }
}

sealed abstract class BFChildOrderType(val v: String)
case object BFLimitOrderType  extends BFChildOrderType("LIMIT")
case object BFMarketOrderType extends BFChildOrderType("MARKET")

sealed abstract class BFOrderSide(val v: String)
case object BFBuy  extends BFOrderSide("BUY")
case object BFSell extends BFOrderSide("SELL")
