package domain.exchange.coincheck

import domain.exchange.coincheck.CCBalance.{BtcAmount, JpyAmount}
import domain.lib.VOFactory
import io.estatico.newtype.macros.newtype
import lib.error.ClientDomainError
import lib.refined.NonNegativeDouble
import zio.IO

final case class CCBalance(jpy: JpyAmount, btc: BtcAmount)

object CCBalance {
  def fromRaw(jpy: Double, btc: Double): IO[ClientDomainError, CCBalance] =
    JpyAmount(jpy).zip(BtcAmount(btc)).map((CCBalance.apply _).tupled)

  @newtype case class JpyAmount(value: NonNegativeDouble)
  object JpyAmount extends VOFactory

  @newtype case class BtcAmount(value: NonNegativeDouble)
  object BtcAmount extends VOFactory
}
