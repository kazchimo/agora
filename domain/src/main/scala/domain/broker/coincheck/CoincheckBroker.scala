package domain.broker.coincheck

import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CCOrder.{CCOrderId, CCOrderRate, LimitOrder}
import domain.exchange.coincheck.{
  CCOrder,
  CCOrderRequest,
  CoincheckExchange,
  Env
}
import lib.error.InternalDomainError
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
    CoincheckExchange with SttpClient with Conf with ZEnv with Logging with Nonce,
    Unit
  ] = CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id)).unit

  def cancelWithWait(
    id: CCOrderId
  ): ZIO[CoincheckExchange with Env, Throwable, Unit] =
    CoincheckExchange.cancelOrder(id) *> CoincheckExchange
      .cancelStatus(id).repeatUntil(identity).unit

  def latestRateRef(initialRate: CCOrderRate): ZIO[
    CoincheckExchange with Env,
    Throwable,
    (UReadOnlyRef[CCOrderRate], UWriteOnlyRef[Boolean])
  ] = for {
    latestRateRef      <- Ref.make(initialRate)
    updateCancelRef    <- Ref.make(false)
    canceledRef        <- Ref.make(false)
    transactionsStream <- CoincheckExchange.publicTransactions
    streamFiber        <-
      transactionsStream
        .foreach(t => latestRateRef.set(CCOrderRate(t.rate.value))).fork
    _                  <- (streamFiber.interruptFork *> canceledRef.set(true))
                            .whenM(updateCancelRef.get).repeatUntilM(_ =>
                              sleep(1.seconds) *> canceledRef.get
                            ).fork
  } yield (latestRateRef.readOnly, updateCancelRef.writeOnly)

  def priceAdjustingOrder(
    orderRequest: CCOrderRequest[LimitOrder],
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging with Conf with Nonce,
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
                                                  s"Cancel order! Reordering... at=${latestRate.toString} amount=${orderRequest.amount.toString}"
                                                )
                                              _          <- cancelWithWait(order.id)
                                              openOrders <- CoincheckExchange.openOrders
                                              openOrder  <-
                                                ZIO
                                                  .fromOption(openOrders.find(_.id == order.id))
                                                  .orElseFail(
                                                    InternalDomainError("Order already canceled")
                                                  )
                                              amount     <-
                                                ZIO
                                                  .fromOption(openOrder.pendingAmount).orElseFail(
                                                    InternalDomainError("Pending amount is not exist")
                                                  )
                                              r          <- priceAdjustingOrder(
                                                              orderRequest
                                                                .changeRate(latestRate).changeAmount(amount),
                                                              intervalSec
                                                            )
                                            } yield r
                                          case _      => log.info("Order settled!").as(order)
                                        }
  } yield result
}
