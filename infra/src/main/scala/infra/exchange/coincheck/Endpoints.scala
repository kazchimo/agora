package infra.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderId

private[coincheck] object Endpoints {
  private def url(s: String) = "https://coincheck.com/api/" + s

  val transactions: String                = url("exchange/orders/transactions")
  val orders: String                      = url("exchange/orders")
  val openOrders: String                  = url("exchange/orders/opens")
  def cancelOrder(id: CCOrderId): String  =
    url(s"exchange/orders/${id.value.value.toString}")
  def cancelStatus(id: CCOrderId): String =
    url(s"exchange/orders/cancel_status?id=${id.value.value.toString}")
  val balance: String                     = url("accounts/balance")
  val websocket: String                   = "wss://ws-api.coincheck.com/"
}
