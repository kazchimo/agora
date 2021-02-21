package domain.exchange.coincheck

import domain.exchange.coincheck.Balance.{BtcAmount, JpyAmount}
import io.estatico.newtype.macros.newtype
import lib.refined.NonNegativeDouble

case class Balance(jpy: JpyAmount, btc: BtcAmount)

object Balance {
  @newtype case class JpyAmount(value: NonNegativeDouble)
  @newtype case class BtcAmount(value: NonNegativeDouble)
}
