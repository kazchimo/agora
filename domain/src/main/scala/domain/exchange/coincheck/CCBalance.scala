package domain.exchange.coincheck

import domain.exchange.coincheck.CCBalance.{BtcAmount, JpyAmount}
import io.estatico.newtype.macros.newtype
import lib.refined.NonNegativeDouble

final case class CCBalance(jpy: JpyAmount, btc: BtcAmount)

object CCBalance {
  @newtype case class JpyAmount(value: NonNegativeDouble)
  @newtype case class BtcAmount(value: NonNegativeDouble)
}
