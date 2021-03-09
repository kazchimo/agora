package domain.exchange.bitflyer

sealed abstract class BFQuantityConditionsEnforcement(val v: String)

case object BFGoodTilCanceledQCE extends BFQuantityConditionsEnforcement("GTC")

case object BFImmediateOrCancelQCE
    extends BFQuantityConditionsEnforcement("IOC")

case object BFFillOrKillQCE extends BFQuantityConditionsEnforcement("FOK")
