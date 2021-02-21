package infra.exchange.coincheck.bodyconverter

import helpers.gen.domain.exchange.coincheck.CCOrderRequestGen._
import infra.exchange.coincheck.bodyconverter.CCOrderConverter._
import io.circe.syntax._
import io.circe.{Json, JsonObject}
import lib.syntax.all._
import zio.random.Random
import zio.test.Assertion._
import zio.test._

object CCOrderRequestConverterTest extends DefaultRunnableSpec {
  val ccOrderRateEncTet
    : Spec[TestConfig with Random, TestFailure[Nothing], TestSuccess] = suite(
    "ccOrderRateEncoder"
  )(
    testM("encode to an inner value")(
      check(ccOrderRateGen)(r =>
        assert(r.asJson)(equalTo(Json.fromDoubleOrNull(r.deepInnerV)))
      )
    )
  )

  val ccOrderAmountEncTest
    : Spec[TestConfig with Random, TestFailure[Nothing], TestSuccess] = suite(
    "ccOrderAmountEncoder"
  )(
    testM("encode to an inner value")(
      check(ccOrderAmountGen)(r =>
        assert(r.asJson)(equalTo(Json.fromDoubleOrNull(r.deepInnerV)))
      )
    )
  )

  val ccOrderRequestEncTest = suite("ccOrderRequestEncoder")(
    testM("buy") {
      check(ccLimitBuyOrderRequestGen) { req =>
        assert(req.asJson)(
          equalTo(
            JsonObject(
              "pair"       -> "btc_jpy".asJson,
              "order_type" -> "buy".asJson,
              "rate"       -> req.rate.get.asJson,
              "amount"     -> req.amount.get.asJson
            ).asJson
          )
        )
      }

    },
    testM("sell") {
      check(ccLimitSellOrderRequestGen) { req =>
        assert(req.asJson)(
          equalTo(
            JsonObject(
              "pair"       -> "btc_jpy".asJson,
              "order_type" -> "sell".asJson,
              "rate"       -> req.rate.get.asJson,
              "amount"     -> req.amount.get.asJson
            ).asJson
          )
        )
      }
    },
    testM("marketBuy") {
      check(ccMarketBuyOrderRequestGen) { req =>
        assert(req.asJson)(
          equalTo(
            JsonObject(
              "pair"              -> "btc_jpy".asJson,
              "order_type"        -> "market_buy".asJson,
              "market_buy_amount" -> req.marketBuyAmount.get.asJson
            ).asJson
          )
        )
      }
    },
    testM("marketSell") {
      check(ccMarketSellOrderRequestGen) { req =>
        assert(req.asJson)(
          equalTo(
            JsonObject(
              "pair"       -> "btc_jpy".asJson,
              "order_type" -> "market_sell".asJson,
              "amount"     -> req.amount.get.asJson
            ).asJson
          )
        )
      }
    }
  )

  override def spec: ZSpec[Environment, Failure] = suite("CCOrderConverter")(
    ccOrderRateEncTet,
    ccOrderAmountEncTest,
    ccOrderRequestEncTest
  )
}
