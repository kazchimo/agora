package domain.exchange.bitflyer

sealed abstract class BFProductCode(
  val code: String,
  val marketType: BFMarketType
)
case object BFBtcJpy    extends BFProductCode("BTC_JPY", BFSpot)
case object BFEthJpy    extends BFProductCode("ETH_JPY", BFSpot)
case object BFFxBtcJpy  extends BFProductCode("FX_BTC_JPY", BFFX)
case object BFEthBtc    extends BFProductCode("ETH_BTC", BFSpot)
case object BFBchBtc    extends BFProductCode("BCH_BTC", BFSpot)
case object BFBtcJpy3M  extends BFProductCode("BTCJPY_MAT3M", BFFutures)
case object BFBtcJpy1WK extends BFProductCode("BTCJPY_MAT1WK", BFFutures)
case object BFBtcJpy2WK extends BFProductCode("BTCJPY_MAT2WK", BFFutures)

sealed abstract class BFMarketType(val v: String)
case object BFSpot    extends BFMarketType("Spot")
case object BFFX      extends BFMarketType("FX")
case object BFFutures extends BFMarketType("Futures")
