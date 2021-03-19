package domain.exchange.coincheck

import cats.syntax.traverse._
import domain.exchange.coincheck.CCOpenOrder.CCOpenOrderType
import domain.exchange.coincheck.CCOpenOrder.CCOpenOrderType.{Buy, Sell}
import domain.exchange.coincheck.CCOrder._
import domain.lib.ZEnum
import enumeratum.EnumEntry.Snakecase
import enumeratum._
import lib.error.{ClientDomainError, InternalDomainError}
import zio.interop.catz.core._
import zio.{IO, ZIO}

final case class CCOpenOrder private (
  id: CCOrderId,
  orderType: CCOpenOrderType,
  rate: Option[CCOrderRate] = None,
  pair: CCOrderPair,
  pendingAmount: Option[CCOrderAmount] = None,
  pendingMarketBuyAmount: Option[CCOrderAmount] = None,
  stopLossRate: Option[CCOrderRate] = None,
  createdAt: CCOrderCreatedAt
) {
  def zioPendingAmount: IO[InternalDomainError, CCOrderAmount] = ZIO
    .fromOption(pendingAmount).orElseFail(
      InternalDomainError("Pending amount is not exist")
    )
}

private[coincheck] trait CCOpenOrderFactory {
  final def fromRaw(
    id: Long,
    orderType: String,
    rate: Option[Double],
    pair: String,
    pendingAmount: Option[Double],
    pendingMarketBuyAmount: Option[Double],
    stopLossRate: Option[Double],
    createdAt: String
  ): IO[ClientDomainError, CCOpenOrder] = for {
    i    <- CCOrderId(id)
    ot   <- CCOpenOrderType.withNameZio(orderType)
    r    <- rate.traverse(CCOrderRate(_))
    p    <- CCOrderPair.withNameZio(pair)
    pa   <- pendingAmount.traverse(CCOrderAmount(_))
    pmba <- pendingMarketBuyAmount.traverse(CCOrderAmount(_))
    slr  <- stopLossRate.traverse(CCOrderRate(_))
    ca   <- CCOrderCreatedAt(createdAt)
  } yield CCOpenOrder(i, ot, r, p, pa, pmba, slr, ca)

  final def buy(
    id: CCOrderId,
    rate: CCOrderRate,
    pair: CCOrderPair,
    createdAt: CCOrderCreatedAt
  ): CCOpenOrder = CCOpenOrder(
    id = id,
    orderType = Buy,
    rate = Some(rate),
    pair = pair,
    createdAt = createdAt
  )

  final def sell(
    id: CCOrderId,
    rate: CCOrderRate,
    pair: CCOrderPair,
    createdAt: CCOrderCreatedAt
  ): CCOpenOrder = CCOpenOrder(
    id = id,
    orderType = Sell,
    rate = Some(rate),
    pair = pair,
    createdAt = createdAt
  )
}

object CCOpenOrder extends CCOpenOrderFactory {
  sealed trait CCOpenOrderType extends Snakecase

  object CCOpenOrderType extends ZEnum[CCOpenOrderType] {
    override def values: IndexedSeq[CCOpenOrderType] = findValues

    case object Buy  extends CCOpenOrderType
    case object Sell extends CCOpenOrderType
  }
}
