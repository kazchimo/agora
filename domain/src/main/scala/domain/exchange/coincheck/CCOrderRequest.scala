package domain.exchange.coincheck

import domain.exchange.coincheck.CCOrderRequest.{
  CCOrderPair,
  CCOrderRequestAmount,
  CCOrderRequestRate,
  CCOrderType
}
import domain.lib.VOFactory
import enumeratum.EnumEntry.Snakecase
import enumeratum._
import eu.timepit.refined.numeric.Positive
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveDouble

import scala.annotation.nowarn

// about order -> https://coincheck.com/ja/documents/exchange/api#order-new
// about stop order -> https://faq.coincheck.com/s/article/40203?language=ja

sealed trait CCOrderRequestKind
@SuppressWarnings(Array("org.wartremover.warts.LeakingSealed"))
trait MarketOrder extends CCOrderRequestKind
@SuppressWarnings(Array("org.wartremover.warts.LeakingSealed"))
trait LimitOrder  extends CCOrderRequestKind

final case class CCOrderRequest[+K <: CCOrderRequestKind] private (
  pair: CCOrderPair,
  orderType: CCOrderType,
  rate: Option[CCOrderRequestRate],
  amount: Option[CCOrderRequestAmount],
  marketBuyAmount: Option[CCOrderRequestAmount],
  stopLossRate: Option[CCOrderRequestRate]
) {
  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitRate[S >: K: =:=[*, LimitOrder]]: CCOrderRequestRate = rate.get

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def limitAmount[S >: K: =:=[*, LimitOrder]]: CCOrderRequestAmount = amount.get

  def changeRate[S >: K: =:=[*, LimitOrder]](
    rate: CCOrderRequestRate
  ): CCOrderRequest[LimitOrder] = this.copy(rate = Some(rate))

  def changeAmount[S >: K: =:=[*, LimitOrder]](
    amount: CCOrderRequestAmount
  ): CCOrderRequest[LimitOrder] = this.copy(amount = Some(amount))
}

object CCOrderRequest {
  sealed trait CCOrderPair extends Snakecase
  object CCOrderPair       extends Enum[CCOrderPair] {
    val values: IndexedSeq[CCOrderPair] = findValues

    case object BtcJpy  extends CCOrderPair
    case object EtcJpy  extends CCOrderPair
    case object FctJpy  extends CCOrderPair
    case object MonaJpy extends CCOrderPair
  }

  sealed trait CCOrderType extends Snakecase
  object CCOrderType       extends Enum[CCOrderType] {
    val values: IndexedSeq[CCOrderType] = findValues

    case object Buy        extends CCOrderType
    case object Sell       extends CCOrderType
    case object MarketBuy  extends CCOrderType
    case object MarketSell extends CCOrderType
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
