package domain.exchange.liquid

import domain.exchange.liquid.Trade.Status.Closed
import domain.exchange.liquid.Trade._
import domain.lib.{VOFactory, ZEnum}
import enumeratum.CirceEnum
import enumeratum.EnumEntry.Snakecase
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveLong

final case class Trade(id: Id, status: Status, pnl: Pnl) {
  def closed: Boolean = status == Closed
}

object Trade {
  @newtype case class Id(value: PositiveLong)
  object Id extends VOFactory

  sealed trait Status extends Snakecase
  object Status       extends ZEnum[Status] {
    override def values: IndexedSeq[Status] = findValues

    case object Open   extends Status
    case object Closed extends Status
  }

  sealed trait Side extends Snakecase
  object Side       extends ZEnum[Side] {
    override def values: IndexedSeq[Side] = findValues

    case object Long  extends Side
    case object Short extends Side
  }

  case class Pnl(value: Double) extends AnyVal

  sealed trait TradingType extends Snakecase
  object TradingType       extends ZEnum[TradingType] with CirceEnum[TradingType] {
    override def values: IndexedSeq[TradingType] = findValues

    case object Cfd       extends TradingType
    case object Margin    extends TradingType
    case object Perpetual extends TradingType
  }
}
