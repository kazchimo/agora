package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCOrder, CoincheckExchange}

final private[coincheck] case class OrderCache() {
  private var ids: List[CCOrderId] = List()
  private var maxId                = 1

  def submitOrder: CCOrder = {
    maxId = maxId + 1
    val id = CCOrderId.unsafeFrom(maxId)
    ids = id +: ids
    CCOrder(id)
  }

  def openOrders: List[CCOrder] = ids.map(CCOrder(_))

  def cancelOrder(orderId: CCOrderId): Unit = {
    ids = ids.filterNot(_ == orderId)
  }

  def closeOrder(orderId: CCOrderId): Unit = cancelOrder(orderId)

  def hasId(id: CCOrderId): Boolean = ids.contains(id)
}

private[coincheck] trait DryCoincheckExchangeImpl
    extends CoincheckExchange.Service {
  protected val cache: OrderCache = OrderCache()
}
