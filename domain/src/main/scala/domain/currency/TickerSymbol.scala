package domain.currency

import domain.DomainError
import zio.{IO, ZIO}

sealed trait TickerSymbol { val value: String }
object TickerSymbol       {
  def apply(v: String): IO[DomainError, TickerSymbol] = v match {
    case BitCoin.value => ZIO.succeed(BitCoin)
    case Jpy.value     => ZIO.succeed(Jpy)
    case _             => ZIO.fail(DomainError(s"invalid ticker symbol string: $v"))
  }
}

case object BitCoin extends TickerSymbol { override val value: String = "btc" }
case object Jpy     extends TickerSymbol { override val value: String = "jpy" }
