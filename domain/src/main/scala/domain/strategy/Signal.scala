package domain.strategy

sealed abstract class Signal(val at: Double) {
  def shouldBuy: Boolean

  final def shouldSell: Boolean = !shouldBuy
}

final case class Buy(override val at: Double) extends Signal(at) {
  override def shouldBuy: Boolean = true
}

final case class Sell(override val at: Double) extends Signal(at) {
  override def shouldBuy: Boolean = false
}
