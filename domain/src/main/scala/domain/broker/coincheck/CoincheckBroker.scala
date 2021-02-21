package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck.{
  CCOrder,
  CCOrderRequest,
  CoincheckExchange,
  LimitOrder
}
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
    orderRequest: CCOrderRequest[LimitOrder],
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging with Conf,
    Throwable,
    CCOrder
  ] = for {
    latestRateRef      <- Ref.make(orderRequest.limitRate)
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
                                    orderRequest.limitRate.value.value * orderRequest.limitAmount.value.value /
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
