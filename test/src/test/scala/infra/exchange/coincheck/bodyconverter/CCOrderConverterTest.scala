package infra.exchange.coincheck.bodyconverter

import helpers.gen.domain.exchange.coincheck.CCOrderGen.{
  ccBuyGen,
  ccMarketBuyAmountGen,
  ccMarketStopBuyGen,
  ccOrderAmountGen,
  ccOrderRateGen
}
import io.circe.Json
import io.circe.syntax._
import zio.test.Assertion._
import zio.test._
import CCOrderConverter._

object CCOrderConverterTest extends DefaultRunnableSpec {
  val ccOrderRateEncTet = suite("ccOrderRateEncoder")(
    testM("encode to an inner value")(
      check(ccOrderRateGen)(r =>
        assert(r.asJson)(equalTo(Json.fromLong(r.value.value)))
      )
    )
  )

  val ccOrderAmountEncTest = suite("ccOrderAmountEncoder")(
    testM("encode to an inner value")(
      check(ccOrderAmountGen)(r =>
        assert(r.asJson)(equalTo(Json.fromDoubleOrNull(r.value.value)))
      )
    )
  )

  val ccMarketBuyAmountEncTest = suite("ccMarketBuyAmountEncoder")(
    testM("encode to an inner value")(
      check(ccMarketBuyAmountGen)(r =>
        assert(r.asJson)(equalTo(Json.fromDoubleOrNull(r.value.value)))
      )
    )
  )

  val ccBuyEncTest = suite("ccBuyEncoder")(
    testM("encode to a corresponding json")(
      check(ccBuyGen)(r =>
        assert(r.asJson)(
          equalTo(
            Json.obj(
              "order_type" -> "buy".asJson,
              "rate"       -> r.rate.asJson,
              "amount"     -> r.amount.asJson,
              "pair"       -> "btc_jpy".asJson
            )
          )
        )
      )
    )
  )

  val ccMarketStopBuyEncTest = suite("ccmarketStopBuyEncoder")(
    testM("encode to a corresponding json")(
      check(ccMarketStopBuyGen)(r =>
        assert(r.asJson)(
          equalTo(
            Json.obj(
              "order_type"        -> "market_buy".asJson,
              "stop_loss_rate"    -> r.stopLossRate.asJson,
              "market_buy_amount" -> r.marketBuyAmount.asJson,
              "pair"              -> "btc_jpy".asJson
            )
          )
        )
      )
    )
  )

  override def spec =
    suite("CCOrderConverter")(
      ccOrderRateEncTet,
      ccOrderAmountEncTest,
      ccMarketBuyAmountEncTest,
      ccBuyEncTest,
      ccMarketStopBuyEncTest
    )
}
