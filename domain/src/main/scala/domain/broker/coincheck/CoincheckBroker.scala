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
import lib.zio.{UReadOnlyRef, UWriteOnlyRef}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.clock.sleep
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
  ): ZIO[CoincheckExchange with Env, Throwable, Unit] =
    CoincheckExchange.cancelOrder(id) *> CoincheckExchange
      .cancelStatus(id).repeatUntil(identity).unit

  def latestRateRef(initialRate: CCOrderRequestRate): ZIO[
    CoincheckExchange with Env,
    Throwable,
    (UReadOnlyRef[CCOrderRequestRate], UWriteOnlyRef[Boolean])
  ] = for {
    latestRateRef      <- Ref.make(initialRate)
    updateCancelRef    <- Ref.make(false)
    canceledRef        <- Ref.make(false)
    transactionsStream <- CoincheckExchange.publicTransactions
    streamFiber        <-
      transactionsStream
        .foreach(t => latestRateRef.set(CCOrderRequestRate(t.rate.value))).fork
    _                  <- (streamFiber.interruptFork *> canceledRef.set(true))
                            .whenM(updateCancelRef.get).repeatUntilM(_ =>
                              sleep(1.seconds) *> canceledRef.get
                            ).fork
  } yield (latestRateRef.readOnly, updateCancelRef.writeOnly)

  def priceAdjustingOrder(
    orderRequest: CCOrderRequest[LimitOrder],
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging with Conf,
    Throwable,
    CCOrder
  ] = for {
    (latestRateRef, updateCancelRef) <- latestRateRef(orderRequest.limitRate)
    order                            <- CoincheckExchange.orders(orderRequest)
    shouldCancel                     <- ZIO
                                          .effectTotal(Should).delay(intervalSec.seconds).race(
                                            waitOrderSettled(order.id).as(ShouldNot)
                                          )
    _                                <- updateCancelRef.set(true)
    result                           <- shouldCancel match {
                                          case Should => for {
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
                                          case _      => log.info("Order settled!").as(order)
                                        }
  } yield result
}
