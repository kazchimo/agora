package domain.exchange.bitflyer

sealed abstract class BFQuantityConditionsEnforcement(val v: String)

case object FBGoodTilCanceledQCE extends BFQuantityConditionsEnforcement("GTC")

case object FBImmediateOrCancelQCE
    extends BFQuantityConditionsEnforcement("IOC")

case object FBFillOrKillQCE extends BFQuantityConditionsEnforcement("FOK")