package infra.exchange.liquid

import domain.exchange.liquid.LiquidOrder.Id
import lib.syntax.all._

private[liquid] object Endpoints {
  val ws = "wss://tap.liquid.com/app/LiquidTapClient:433"

  val root = "https://api.liquid.com"

  val ordersPath = "/orders"
  val orders     = root + ordersPath

  def cancelOrder(id: Id) = ordersPath + s"/${id.deepInnerV.toString}/cancel"
}
