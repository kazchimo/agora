package infra.exchange.liquid

private[liquid] object Endpoints {
  val ws = "wss://tap.liquid.com/app/LiquidTapClient:433"

  val root = "https://api.liquid.com"

  val ordersPath = "/orders"
  val orders     = root + ordersPath
}
