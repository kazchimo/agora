package helpers.gen.domain.exchange.coincheck

import domain.exchange.coincheck.CCOrderRequest.CCOrderType.{
  Buy,
  MarketBuy,
  MarketSell,
  Sell
}
import domain.exchange.coincheck.CCOrderRequest._
import domain.exchange.coincheck._
import helpers.gen.std.StdGen.positiveDoubleGen
import zio.random.Random
import zio.test.{Gen, Sized}

object CCOrderRequestGen {
  val ccOrderRateGen: Gen[Random, CCOrderRequestRate] =
    positiveDoubleGen.map(CCOrderRequestRate.unsafeFrom)

  val ccOrderAmountGen: Gen[Random, CCOrderRequestAmount] =
    positiveDoubleGen.map(CCOrderRequestAmount.unsafeFrom)

  val ccLimitBuyOrderRequestGen: Gen[Random, CCOrderRequest[Buy]] =
    Gen.zipN(ccOrderRateGen, ccOrderAmountGen)(CCOrderRequest.limitBuy)

  val ccLimitSellOrderRequestGen: Gen[Random, CCOrderRequest[Sell]] =
    Gen.zipN(ccOrderRateGen, ccOrderAmountGen)(CCOrderRequest.limitSell)

  val ccMarketBuyOrderRequestGen: Gen[Random, CCOrderRequest[MarketBuy]] =
    ccOrderAmountGen.map(CCOrderRequest.marketBuy)

  val ccMarketSellOrderRequestGen: Gen[Random, CCOrderRequest[MarketSell]] =
    ccOrderAmountGen.map(CCOrderRequest.marketSell)

  val ccOrderRequestGen: Gen[Random with Sized, CCOrderRequest[CCOrderType]] =
    Gen.oneOf(
      ccLimitBuyOrderRequestGen,
      ccLimitSellOrderRequestGen,
      ccMarketBuyOrderRequestGen,
      ccMarketSellOrderRequestGen
    )
}
