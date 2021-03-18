package domain.strategy

import lib.refined.PositiveDouble

sealed abstract class Signal(val at: PositiveDouble) {
  def shouldBuy: Boolean

  final def shouldSell: Boolean = !shouldBuy
}

final case class Buy(override val at: PositiveDouble) extends Signal(at) {
  override def shouldBuy: Boolean = true
}

final case class Sell(override val at: PositiveDouble) extends Signal(at) {
  override def shouldBuy: Boolean = false
}
