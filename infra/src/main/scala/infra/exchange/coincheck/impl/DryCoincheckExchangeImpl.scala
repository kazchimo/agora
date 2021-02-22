package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder.CCOrderType.{
  Buy,
  MarketBuy,
  MarketSell,
  Sell
}
import domain.exchange.coincheck.CCOrder.{
  CCOrderCreatedAt,
  CCOrderId,
  CCOrderRate,
  CCOrderType,
  LimitOrder
}
import domain.exchange.coincheck._
import lib.syntax.all._
import zio.{Task, ZIO}

import java.time.ZonedDateTime

final private[impl] case class FakeBalance(jpy: Double, btc: Double) {
  def rebalanceWithDiff(jpy: Double, btc: Double): FakeBalance =
    FakeBalance(this.jpy + jpy, this.btc + btc)
}

final private[impl] case class FakeExchange(marketRate: CCOrderRate) {
  private var pendingOrders: Map[CCOrderId, CCOrderRequest[_ <: CCOrderType]] =
    Map.empty
  private var balance                                                         = FakeBalance(0, 0)
  private var maxId                                                           = 1

  def submitOrder(limitOrder: CCOrderRequest[_ <: CCOrderType]): CCOrder = {
    maxId = maxId + 1
    val id = CCOrderId.unsafeFrom(maxId)
    pendingOrders = pendingOrders.+(id -> limitOrder)
    CCOrder(id)
  }

  def btc: Double = balance.btc

  def jpy: Double = balance.jpy

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  def openOrders: Seq[CCOpenOrder] = pendingOrders.keys.map { key =>
    val req = pendingOrders(key)
    req.orderType match {
      case Buy  =>
        val e = req.asInstanceOf[CCOrderRequest[Buy]]
        CCOpenOrder.buy(
          key,
          e.limitRate,
          e.pair,
          CCOrderCreatedAt.unsafeFrom(ZonedDateTime.now().toString)
        )
      case Sell =>
        val e = req.asInstanceOf[CCOrderRequest[Sell]]
        CCOpenOrder.sell(
          key,
          e.limitRate,
          e.pair,
          CCOrderCreatedAt.unsafeFrom(ZonedDateTime.now().toString)
        )
    }
  }.toSeq

  def removeOrder(orderId: CCOrderId): Unit = {
    pendingOrders = pendingOrders.-(orderId)
  }

  def cancelOrder(orderId: CCOrderId): Unit = removeOrder(orderId)

  @SuppressWarnings(Array("org.wartremover.warts.All"))
  def closeOrder(orderId: CCOrderId): Task[Unit] = for {
    request <- ZIO.effect(pendingOrders(orderId))
    _       <- ZIO.effect {
                 request.orderType match {
                   case Buy        => rebalanceWithDiff(
                       request.amount.get.deepInnerV,
                       -request.asInstanceOf[CCOrderRequest[LimitOrder]].jpy
                     )
                   case MarketBuy  => rebalanceWithDiff(
                       request.marketBuyAmount.get.deepInnerV / marketRate.deepInnerV,
                       -request.marketBuyAmount.get.deepInnerV
                     )
                   case Sell       => rebalanceWithDiff(
                       -request.amount.get.deepInnerV,
                       request.asInstanceOf[CCOrderRequest[LimitOrder]].jpy
                     )
                   case MarketSell => rebalanceWithDiff(
                       -request.amount.get.deepInnerV,
                       marketRate.deepInnerV * request.amount.get.deepInnerV
                     )
                 }
               }
    _       <- ZIO.effect(removeOrder(orderId))
  } yield ()

  def submitted(id: CCOrderId): Boolean = pendingOrders.keys.toSeq.contains(id)

  private def rebalanceWithDiff(btc: Double, jpy: Double): Unit =
    balance = balance.rebalanceWithDiff(jpy, btc)
}

abstract private[coincheck] class DryCoincheckExchangeImpl(
  marketRate: CCOrderRate
) extends CoincheckExchange.Service {
  protected val fakeExchange: FakeExchange = FakeExchange(marketRate)
}
