package domain.exchange.liquid

import domain.exchange.liquid.Trade._
import domain.lib.{VOFactory, ZEnum}
import enumeratum.EnumEntry.Snakecase
import io.estatico.newtype.macros.newtype
import lib.refined.PositiveLong

final case class Trade(id: Id, status: Status)

object Trade {
  @newtype case class Id(value: PositiveLong)
  object Id extends VOFactory

  sealed trait Status extends Snakecase
  object Status       extends ZEnum[Status] {
    override def values: IndexedSeq[Status] = findValues

    case object Open   extends Status
    case object Closed extends Status
  }
}
