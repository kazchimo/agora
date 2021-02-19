package helpers.gen.domain.exchange.coincheck

import domain.exchange.coincheck.CCLimitOrderRequest.{
  CCOrderRequestAmount,
  CCOrderRequestRate
}
import domain.exchange.coincheck.CCMarketBuyRequest.CCMarketBuyRequestAmount
import domain.exchange.coincheck._
import helpers.gen.std.StdGen.positiveDoubleGen
import zio.random.Random
import zio.test.Gen

object CCOrderGen {
  val ccOrderRateGen: Gen[Random, CCOrderRequestRate] =
    positiveDoubleGen.map(CCOrderRequestRate.unsafeFrom)

  val ccOrderAmountGen: Gen[Random, CCOrderRequestAmount] =
    positiveDoubleGen.map(CCOrderRequestAmount.unsafeFrom)

  val ccMarketBuyAmountGen: Gen[Random, CCMarketBuyRequestAmount] =
    positiveDoubleGen.map(CCMarketBuyRequestAmount.unsafeFrom)

  val ccBuyGen: Gen[Random, CCLimitBuyRequest] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCLimitBuyRequest(_, _))

  val ccStopBuyGen: Gen[Random, CCLimitStopBuyRequest] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(
      CCLimitStopBuyRequest
    )

  val ccSellGen: Gen[Random, CCLimitSellRequest] =
    ccOrderRateGen.crossWith(ccOrderAmountGen)(CCLimitSellRequest(_, _))

  val ccStopSellGen: Gen[Random, CCLimitStopSellRequest] =
    Gen.zipN(ccOrderRateGen, ccOrderRateGen, ccOrderAmountGen)(
      CCLimitStopSellRequest
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
