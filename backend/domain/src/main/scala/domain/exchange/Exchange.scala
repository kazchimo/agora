package domain.exchange

sealed trait Exchange
trait Coincheck extends Exchange
trait Liquid    extends Exchange
