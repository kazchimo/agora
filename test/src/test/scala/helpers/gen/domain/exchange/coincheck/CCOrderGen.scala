package helpers.gen.domain.exchange.coincheck

import domain.exchange.coincheck._
import domain.exchange.coincheck.CCMarketBuy.CCMarketBuyAmount
import domain.exchange.coincheck.CCOrder.{CCOrderAmount, CCOrderRate}
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

  val ccBuyGen: Gen[Random, CCBuy] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCBuy)

  val ccStopBuyGen: Gen[Random, CCStopBuy] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(CCStopBuy)

  val ccSellGen: Gen[Random, CCSell] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCSell)

  val ccStopSellGen: Gen[Random, CCStopSell] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(CCStopSell)

  val ccMarketBuyGen: Gen[Random, CCMarketBuy] =
    ccMarketBuyAmountGen.map(CCMarketBuy(_))

  val ccMarketStopBuyGen: Gen[Random, CCMarketStopBuy] =
    ccOrderRateGen.crossWith(ccMarketBuyAmountGen)(CCMarketStopBuy)

  val ccMarketSellGen: Gen[Random, CCMarketSell] =
    ccOrderAmountGen.map(CCMarketSell)

  val ccMarketStopSellGen: Gen[Random, CCMarketStopSell] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCMarketStopSell)

  val ccOrderGen: Gen[Random, CCOrder] = Gen.oneOf(
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
