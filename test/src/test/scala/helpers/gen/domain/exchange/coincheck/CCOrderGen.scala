package helpers.gen.domain.exchange.coincheck

import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyAmount
import domain.exchange.coincheck.CCOrderRequest.{CCOrderAmount, CCOrderRate}
import domain.exchange.coincheck._
import helpers.gen.std.StdGen.{positiveDoubleGen, positiveLongGen}
import zio.random.Random
import zio.test.Gen

object CCOrderGen {
  val ccOrderRateGen: Gen[Random, CCOrderRate] =
    positiveLongGen.map(CCOrderRate.unsafeFrom)

  val ccOrderAmountGen: Gen[Random, CCOrderAmount] =
    positiveDoubleGen.map(CCOrderAmount.unsafeFrom)

  val ccMarketBuyAmountGen: Gen[Random, CCMarketBuyAmount] =
    positiveDoubleGen.map(CCMarketBuyAmount.unsafeFrom)

  val ccBuyGen: Gen[Random, CCBuyRequest] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCBuyRequest)

  val ccStopBuyGen: Gen[Random, CCStopBuyRequest] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(CCStopBuyRequest)

  val ccSellGen: Gen[Random, CCSellRequest] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCSellRequest)

  val ccStopSellGen: Gen[Random, CCStopSellRequest] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(
      CCStopSellRequest
    )

  val ccMarketBuyGen: Gen[Random, CCMarketBuyRequest] =
    ccMarketBuyAmountGen.map(CCMarketBuyRequest(_))

  val ccMarketStopBuyGen: Gen[Random, CCMarketStopBuyRequest] =
    ccOrderRateGen.crossWith(ccMarketBuyAmountGen)(CCMarketStopBuyRequest)

  val ccMarketSellGen: Gen[Random, CCMarketSellRequest] =
    ccOrderAmountGen.map(CCMarketSellRequest)

  val ccMarketStopSellGen: Gen[Random, CCMarketStopSellRequest] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCMarketStopSellRequest)

  val ccOrderGen: Gen[Random, CCOrderRequest] = Gen.oneOf(
    ccBuyGen,
    ccStopBuyGen,
    ccSellGen,
    ccStopSellGen,
    ccMarketBuyGen,
    ccMarketStopBuyGen,
    ccMarketSellGen,
    ccMarketStopSellGen
  )
}
