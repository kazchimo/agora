package infra.exchange.coincheck

private[coincheck] object Endpoints {
  private def url(s: String) = "https://coincheck.com/api/" + s

  val transactions: String = url("exchange/orders/transactions")
  val orders: String       = url("exchange/orders")
}
