package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrderRequest.CCOrderPair.BtcJpy
import domain.exchange.coincheck.CCOrderRequest.CCOrderType.{Buy, Sell}
import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderPair,
  CCOrderRequestAmount,
  CCOrderRequestRate,
  CCOrderType,
  LimitOrder
}
import domain.lib.VOFactory
import enumeratum.EnumEntry.Snakecase
import enumeratum._
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.error.ClientDomainError
import lib.refined.PositiveDouble
import zio.{IO, ZIO}

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

final case class CCOrderRequest[+T <: CCOrderType] private (
  pair: CCOrderPair,
  orderType: T,
  rate: Option[CCOrderRequestRate],
  amount: Option[CCOrderRequestAmount],
  marketBuyAmount: Option[CCOrderRequestAmount],
  stopLossRate: Option[CCOrderRequestRate]
) {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitRate[S >: T: <:<[*, LimitOrder]]: CCOrderRequestRate = rate.get

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitAmount[S >: T: <:<[*, LimitOrder]]: CCOrderRequestAmount = amount.get

  def changeRate[S >: T](
    rate: CCOrderRequestRate
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(rate = Some(rate), orderType = ev(orderType))

  def changeAmount[S >: T](
    amount: CCOrderRequestAmount
  )(implicit ev: <:<[S, LimitOrder]): CCOrderRequest[LimitOrder] =
    this.copy(amount = Some(amount), orderType = ev(orderType))

  def jpy[S >: T: =:=[*, LimitOrder]]: Double =
    limitAmount.value.value * limitRate.value.value
}

private[coincheck] trait OrderFactory {
  final def limitBuy(
    rate: Double,
    amount: Double
  ): IO[ClientDomainError, CCOrderRequest[Buy.type]] = for {
    r <- CCOrderRequestRate(rate)
    a <- CCOrderRequestAmount(amount)
  } yield CCOrderRequest(BtcJpy, Buy, Some(r), Some(a), None, None)

  final def limitSell(
    rate: Double,
    amount: Double
  ): IO[ClientDomainError, CCOrderRequest[Sell.type]] = for {
    r <- CCOrderRequestRate(rate)
    a <- CCOrderRequestAmount(amount)
  } yield CCOrderRequest(BtcJpy, Sell, Some(r), Some(a), None, None)
}

object CCOrderRequest extends OrderFactory {
  sealed trait CCOrderPair extends Snakecase
  object CCOrderPair       extends Enum[CCOrderPair] with CirceEnum[CCOrderPair] {
    val values: IndexedSeq[CCOrderPair] = findValues

    case object BtcJpy  extends CCOrderPair
    case object EtcJpy  extends CCOrderPair
    case object FctJpy  extends CCOrderPair
    case object MonaJpy extends CCOrderPair
  }

  sealed trait CCOrderType extends Snakecase
  sealed trait LimitOrder  extends CCOrderType
  sealed trait MarketOrder extends CCOrderType

  object CCOrderType extends Enum[CCOrderType] with CirceEnum[CCOrderType] {
    val values: IndexedSeq[CCOrderType] = findValues

    case object Buy        extends LimitOrder
    case object Sell       extends LimitOrder
    case object MarketBuy  extends MarketOrder
    case object MarketSell extends MarketOrder
  }

  @newtype case class CCOrderRequestRate(value: PositiveDouble)
  object CCOrderRequestRate extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestRate
  }

  @newtype case class CCOrderRequestAmount(value: PositiveDouble)
  object CCOrderRequestAmount extends VOFactory[Double, Positive] {
    override type VO = CCOrderRequestAmount
  }
}
