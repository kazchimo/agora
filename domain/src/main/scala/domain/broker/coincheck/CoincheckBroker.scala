package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.coincheck.CCLimitOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{CCLimitOrderRequest, CCOrder, CoincheckExchange}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.duration._
import zio.logging.{Logging, log}
import zio.{RIO, Ref, ZEnv, ZIO}

sealed private[coincheck] trait ShouldCancel extends Product with Serializable
private[coincheck] case object Should        extends ShouldCancel
private[coincheck] case object ShouldNot     extends ShouldCancel

final case class CoincheckBroker() {
  def waitOrderSettled(
    id: CCOrderId
  ): RIO[CoincheckExchange with SttpClient with Conf, Unit] =
    CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id)).unit

  def priceAdjustingOrder(
    orderRequest: CCLimitOrderRequest,
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging with Conf,
    Throwable,
    CCOrder
  ] = for {
    latestRateRef      <- Ref.make(orderRequest.rate)
    settledRef         <- Ref.make(false)
    transactionsStream <- CoincheckExchange.publicTransactions
    transactionFiber   <- transactionsStream
                            .foreach(t =>
                              latestRateRef.set(
                                CCOrderRequestRate(t.rate.value)
                              ) *> settledRef.get.map(!_)
                            ).fork
    order              <- CoincheckExchange.orders(orderRequest)
    shouldCancel       <- ZIO
                            .effectTotal(Should).delay(intervalSec.seconds).race(
                              waitOrderSettled(order.id).as(ShouldNot)
                            )
    result             <- shouldCancel match {
                            case Should => for {
                                _          <- transactionFiber.interruptFork
                                latestRate <- latestRateRef.get
                                amount     <-
                                  CCOrderRequestAmount(
                                    orderRequest.rate.value.value * orderRequest.amount.value.value /
                                      latestRate.value.value
                                  )
                                newOrderReq =
                                  orderRequest.changeRate(latestRate).changeAmount(amount)
                                _          <-
                                  log.info(
                                    s"Cancel order! Reordering... order=${newOrderReq.toString}"
                                  )
                                _          <- CoincheckExchange.cancelOrder(order.id)
                                _          <- CoincheckExchange
                                                .cancelStatus(order.id).repeatUntil(identity)
                                r          <- priceAdjustingOrder(newOrderReq, intervalSec)
                              } yield r
                            case _      => transactionFiber.interruptFork *>
                                log.info("Order settled!").as(order) <* settledRef.set(true)
                          }
  } yield result
}
