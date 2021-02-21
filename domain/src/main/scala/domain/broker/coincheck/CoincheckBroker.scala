package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.CCOrderRequest.{CCOrderRequestRate, LimitOrder}
import domain.exchange.coincheck.{
  CCOrder,
  CCOrderRequest,
  CoincheckExchange,
  Env
}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.duration._
import zio.logging.{Logging, log}
import zio.{RIO, Ref, ZEnv, ZIO}

sealed private[coincheck] trait ShouldCancel extends Product with Serializable
private[coincheck] case object Should        extends ShouldCancel
private[coincheck] case object ShouldNot     extends ShouldCancel

final case class CoincheckBroker() {
  def waitOrderSettled(id: CCOrderId): RIO[
    CoincheckExchange with SttpClient with Conf with ZEnv with Logging,
    Unit
  ] = CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id)).unit

  def cancelWithWait(
    id: CCOrderId
  ): ZIO[CoincheckExchange with Env, Throwable, Boolean] = CoincheckExchange
    .cancelOrder(id) *> CoincheckExchange.cancelStatus(id).repeatUntil(identity)

  def priceAdjustingOrder(
    orderRequest: CCOrderRequest[LimitOrder],
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging with Conf,
    Throwable,
    CCOrder
  ] = for {
    latestRateRef      <- Ref.make(orderRequest.limitRate)
    transactionsStream <- CoincheckExchange.publicTransactions
    transactionFiber   <-
      transactionsStream
        .foreach(t => latestRateRef.set(CCOrderRequestRate(t.rate.value))).fork
    order              <- CoincheckExchange.orders(orderRequest)
    shouldCancel       <- ZIO
                            .effectTotal(Should).delay(intervalSec.seconds).race(
                              waitOrderSettled(order.id).as(ShouldNot)
                            )
    result             <- shouldCancel match {
                            case Should => for {
                                _          <- transactionFiber.interruptFork
                                latestRate <- latestRateRef.get
                                _          <-
                                  log.info(
                                    s"Cancel order! Reordering... at=${latestRate.toString}"
                                  )
                                _          <- cancelWithWait(order.id)
                                r          <- priceAdjustingOrder(
                                                orderRequest.changeRate(latestRate),
                                                intervalSec
                                              )
                              } yield r
                            case _      => transactionFiber.interruptFork *>
                                log.info("Order settled!").as(order)
                          }
  } yield result
}
