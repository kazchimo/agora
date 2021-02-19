package domain.broker.coincheck

import domain.exchange.coincheck.CCLimitOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck.CCOrder.CCOrderId
import domain.exchange.coincheck.{
  CCLimitOrderRequest,
  CCOrder,
  CCPublicTransaction,
  CoincheckExchange
}
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.duration._
import zio.logging.{Logging, log}
import zio.stream.UStream
import zio.{RIO, Ref, ZEnv, ZIO}

sealed private[coincheck] trait ShouldCancel extends Product with Serializable
private[coincheck] case object Should        extends ShouldCancel
private[coincheck] case object ShouldNot     extends ShouldCancel

final case class CoincheckBroker(tras: UStream[CCPublicTransaction]) {
  def waitOrderSettled(
    id: CCOrderId
  ): RIO[CoincheckExchange with SttpClient, Unit] =
    CoincheckExchange.openOrders.repeatWhile(_.map(_.id).contains(id)).unit

  def priceAdjustingOrder(
    orderRequest: CCLimitOrderRequest,
    intervalSec: Int
  ): ZIO[
    SttpClient with CoincheckExchange with ZEnv with Logging,
    Throwable,
    CCOrder
  ] = for {
    latestRateRef <- Ref.make(orderRequest.rate)
    _             <-
      tras
        .foreach(t => latestRateRef.set(CCOrderRequestRate(t.rate.value))).fork
    order         <- CoincheckExchange.orders(orderRequest)
    shouldCancel  <-
      ZIO
        .effectTotal(Should).delay(intervalSec.seconds).race(
          waitOrderSettled(order.id).as(ShouldNot) *> log.info("Order settled!")
        )
    result        <- shouldCancel match {
                       case Should => for {
                           _          <- log.info("Cancel order! Reordering...")
                           _          <- CoincheckExchange.cancelOrder(order.id)
                           _          <- CoincheckExchange
                                           .cancelStatus(order.id).repeatUntil(identity)
                           latestRate <- latestRateRef.get
                           amount     <-
                             CCOrderRequestAmount(
                               orderRequest.rate.value.value * orderRequest.amount.value.value /
                                 latestRate.value.value
                             )
                           r          <- priceAdjustingOrder(
                                           orderRequest
                                             .changeRate(latestRate).changeAmount(amount),
                                           intervalSec
                                         )
                         } yield r
                       case _      => ZIO.succeed(order)
                     }
  } yield result
}
