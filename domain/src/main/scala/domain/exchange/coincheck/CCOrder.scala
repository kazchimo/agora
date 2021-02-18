package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.lib.VOFactory
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype

final case class CCOrder(id: CCOrderId)

object CCOrder {
  @newtype case class CCOrderId(value: Refined[Long, Positive])
  object CCOrderId extends VOFactory[Long, Positive] {
    override type VO = CCOrderId
  }
}
