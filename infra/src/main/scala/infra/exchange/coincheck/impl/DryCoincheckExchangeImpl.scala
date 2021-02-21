package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CCOrderRequest.CCOrderType._
import domain.exchange.coincheck.CCOrderRequest.LimitOrder
import domain.exchange.coincheck._
import zio.{Task, ZIO}

import scala.util.Random

final private[coincheck] case class OrderCache() {
  private var ids: Seq[CCOrderId]                              = Seq.empty[CCOrderId]
  private var pendingOrders: Map[CCOrderId, CCOrderRequest[_]] = Map.empty
  private var balance: Map[String, Double]                     = Map("jpy" -> 0, "btc" -> 0)
  private var maxId                                            = 1

  def submitOrder(limitOrder: CCOrderRequest[_]): CCOrder = {
    maxId = maxId + 1
    val id = CCOrderId.unsafeFrom(maxId)
    ids = id +: ids
    pendingOrders = pendingOrders.+(id -> limitOrder)
    CCOrder(id)
  }

  def openOrders: Seq[CCOrder] = ids.map(CCOrder(_))

  def cancelOrder(orderId: CCOrderId): Unit = {
    ids = ids.filterNot(_ == orderId)
  }

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  def closeOrder(orderId: CCOrderId): Task[Unit] = for {
    request <- ZIO.effect(pendingOrders(orderId))
    _       <- ZIO.effect {
                 request.orderType match {
                   case Buy        => rebalanceWithDiff(
                       request.amount.get.value.value,
                       -request.asInstanceOf[CCOrderRequest[LimitOrder]].jpy
                     )
                   case MarketBuy  => rebalanceWithDiff(
                       Random.between(0, Double.MaxValue),
                       -request.marketBuyAmount.get.value.value
                     )
                   case Sell       => rebalanceWithDiff(
                       -request.amount.get.value.value,
                       request.asInstanceOf[CCOrderRequest[LimitOrder]].jpy
                     )
                   case MarketSell => rebalanceWithDiff(
                       -Random.between(
                         0,
                         Double.MaxValue
                       ) * request.amount.get.value.value,
                       request.amount.get.value.value
                     )
                 }
               }
    _       <- ZIO.effect {
                 ids = ids.filterNot(_ == orderId)
               }
  } yield ()

  def hasId(id: CCOrderId): Boolean = ids.contains(id)

  private def rebalanceWithDiff(btc: Double, jpy: Double): Unit =
    balance = balance
      .updated("btc", balance("btc") + btc).updated("jpy", balance("jpy") + jpy)
}

private[coincheck] trait DryCoincheckExchangeImpl
    extends CoincheckExchange.Service {
  protected val cache: OrderCache = OrderCache()
}
