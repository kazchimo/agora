package domain.exchange.coincheck

import domain.exchange.coincheck.CCBalance.{BtcAmount, JpyAmount}
import domain.lib.VOFactory
import eu.timepit.refined.numeric.NonNegative
import io.estatico.newtype.macros.newtype
import lib.error.ClientDomainError
import lib.refined.NonNegativeDouble
import zio.IO

final case class CCBalance(jpy: JpyAmount, btc: BtcAmount)

object CCBalance {
  def fromRaw(jpy: Double, btc: Double): IO[ClientDomainError, CCBalance] =
    for {
      jpy <- JpyAmount(jpy)
      btc <- BtcAmount(btc)
    } yield CCBalance(jpy, btc)

  @newtype case class JpyAmount(value: NonNegativeDouble)
  object JpyAmount extends VOFactory[Double, NonNegative] {
    override type VO = JpyAmount
  }

  @newtype case class BtcAmount(value: NonNegativeDouble)
  object BtcAmount extends VOFactory[Double, NonNegative] {
    override type VO = BtcAmount
  }
}
