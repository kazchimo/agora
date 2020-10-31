package domain.exchange.coincheck

sealed trait Order

final case class Buy() extends Order

final case class Sell() extends Order

final case class MarketBuy() extends Order

final case class MarketSell() extends Order

final case class MarketStopSell() extends Order
