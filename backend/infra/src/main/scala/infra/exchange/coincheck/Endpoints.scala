package infra.exchange.coincheck

import domain.exchange.coincheck.CCOrder.CCOrderId
import lib.syntax.all._

private[coincheck] object Endpoints {
  private def url(s: String) = "https://coincheck.com/api/" + s

  val transactions: String                = url("exchange/orders/transactions")
  val orders: String                      = url("exchange/orders")
  val openOrders: String                  = url("exchange/orders/opens")
  def cancelOrder(id: CCOrderId): String  =
    url(s"exchange/orders/${id.deepInnerV.toString}")
  def cancelStatus(id: CCOrderId): String =
    url(s"exchange/orders/cancel_status?id=${id.deepInnerV.toString}")
  val balance: String                     = url("accounts/balance")
  val websocket: String                   = "wss://ws-api.coincheck.com/"
}
