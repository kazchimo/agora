package domain.broker.coincheck

import domain.AllEnv
import domain.exchange.coincheck.CCOrder.{CCOrderId, CCOrderRate, LimitOrder}
import domain.exchange.coincheck.{
  CCOpenOrder,
  CCOrder,
  CCOrderRequest,
  CoincheckExchange
}
import lib.error.ClientDomainError
import lib.zio.{UReadOnlyRef, UWriteOnlyRef}
import zio.clock.sleep
import zio.duration._
import zio.logging.log
import zio.{RIO, Ref, ZIO}

sealed private[coincheck] trait ShouldCancel extends Product with Serializable
private[coincheck] case object Should        extends ShouldCancel
private[coincheck] case object ShouldNot     extends ShouldCancel

final case class CoincheckBroker() {
  def waitOrderSettled(id: CCOrderId): RIO[AllEnv, Unit] =
    CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id)).unit

  def cancelWithWait(id: CCOrderId): ZIO[AllEnv, Throwable, Unit] =
    CoincheckExchange.cancelOrder(id) *> CoincheckExchange
      .cancelStatus(id).repeatUntil(identity).unit

  def latestRateRef(initialRate: CCOrderRate): ZIO[
    AllEnv,
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

  def findOpenOrder(
    id: CCOrderId
  ): ZIO[AllEnv, ClientDomainError, CCOpenOrder] = CoincheckExchange.openOrders
    .flatMap(o => ZIO.fromOption(o.find(_.id == id))).orElseFail(
      ClientDomainError("Order already settled")
    )

  def priceAdjustingOrder(
    orderRequest: CCOrderRequest[LimitOrder],
    intervalSec: Int
  ): ZIO[AllEnv, Throwable, CCOrder] = for {
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
                                              openOrder  <- findOpenOrder(order.id)
                                              _          <- cancelWithWait(order.id)
                                              amount     <- openOrder.zioPendingAmount
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
