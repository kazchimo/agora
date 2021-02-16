package infra.exchange.coincheck.responses

import domain.currency.{BitCoin, Currency, Jpy, TickerSymbol}
import domain.exchange.coincheck.CCTransaction
import domain.exchange.coincheck.CCTransaction.CCTraRate
import helpers.gen.infra.exchange.coincheck.responses.TransactionResponseGen.{
  failedTransactionsResponseGen,
  transactionResponseGen,
  transactionsResponseGen
}
import helpers.gen.std.StdGen.negativeDoubleGen
import infra.InfraError
import io.circe.parser.decode
import io.scalaland.chimney.dsl._
import zio.Task
import zio.test.Assertion._
import zio.test._

object TransactionsResponseTest extends DefaultRunnableSpec {
  override def spec =
    suite("TransactionsResponse")(transformerTest, decoderTest)

  val transformerTest = suite("transformer to Task[List[CCTransaction]]")(
    testM("transform into CCTransactions from SuccessTransactionsResponse")(
      checkM(transactionsResponseGen)(t =>
        assertM(t.transformInto[Task[List[CCTransaction]]])(
          isSubtype[List[CCTransaction]](anything)
        )
      )
    ),
    testM("transform into a error")(
      checkM(failedTransactionsResponseGen)(t =>
        assertM(t.transformInto[Task[List[CCTransaction]]].run)(
          fails(isSubtype[InfraError](hasMessage(containsString("error"))))
        )
      )
    )
  )

  val successJson =
    "{\n  \"success\": true,\n  \"transactions\": [\n    {\n      \"id\": 38,\n      \"order_id\": 49,\n      \"created_at\": \"2015-11-18T07:02:21.000Z\",\n      \"funds\": {\n        \"btc\": \"0.1\",\n        \"jpy\": \"-4096.135\"\n      },\n      \"pair\": \"btc_jpy\",\n      \"rate\": \"40900.0\",\n      \"fee_currency\": \"JPY\",\n      \"fee\": \"6.135\",\n      \"liquidity\": \"T\",\n      \"side\": \"buy\"\n    },\n    {\n      \"id\": 37,\n      \"order_id\": 48,\n      \"created_at\": \"2015-11-18T07:02:21.000Z\",\n      \"funds\": {\n        \"btc\": \"-0.1\",\n        \"jpy\": \"4094.09\"\n      },\n      \"pair\": \"btc_jpy\",\n      \"rate\": \"40900.0\",\n      \"fee_currency\": \"JPY\",\n      \"fee\": \"-4.09\",\n      \"liquidity\": \"M\",\n      \"side\": \"sell\"\n    }\n  ]\n}"
  val failJson    = "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  val decoderTest = suite("decoder")(
    test("decode a success json to a SuccessTransactionsResponse")(
      assert(decode[TransactionsResponse](successJson))(
        isRight(isSubtype[SuccessTransactionsResponse](anything))
      )
    ),
    test("decode a fail json to a FailedTransactionsResponse")(
      assert(decode[TransactionsResponse](failJson))(
        isRight(isSubtype[FailedTransactionsResponse](anything))
      )
    )
  )
}

object TransactionResponseTest extends DefaultRunnableSpec {
  override def spec = suite("TransactionResponse")(
    sellCurrencySuite,
    buyCurrencySuite,
    dRateSuite
  )

  val sellCurrencySuite = suite("#sellCurrency")(
    testM("fails with invalid side")(
      checkM(transactionResponseGen.map(_.copy(side = "hoge")))(t =>
        assertM(t.sellCurrency.run)(fails(anything))
      )
    ),
    testM("returns BTC Currency if side is Sell")(
      checkM(transactionResponseGen.map(_.copy(side = "sell")))(t =>
        assertM(t.sellCurrency)(
          isSubtype(
            hasField[Currency, TickerSymbol](
              "tickerSymbol",
              (a: Currency) => a.tickerSymbol,
              equalTo(BitCoin)
            )
          )
        )
      )
    ),
    testM("returns Jpy Currency if side is Buy")(
      checkM(transactionResponseGen.map(_.copy(side = "buy")))(t =>
        assertM(t.sellCurrency)(
          isSubtype(
            hasField[Currency, TickerSymbol](
              "tickerSymbol",
              (a: Currency) => a.tickerSymbol,
              equalTo(Jpy)
            )
          )
        )
      )
    )
  )

  val buyCurrencySuite = suite("#buyCurrency")(
    testM("fails with invalid side")(
      checkM(transactionResponseGen.map(_.copy(side = "hoge")))(t =>
        assertM(t.sellCurrency.run)(fails(anything))
      )
    ),
    testM("returns BTC Currency if side is Buy")(
      checkM(transactionResponseGen.map(_.copy(side = "buy")))(t =>
        assertM(t.buyCurrency)(
          isSubtype(
            hasField[Currency, TickerSymbol](
              "tickerSymbol",
              (a: Currency) => a.tickerSymbol,
              equalTo(BitCoin)
            )
          )
        )
      )
    ),
    testM("returns Jpy Currency if side is Sell")(
      checkM(transactionResponseGen.map(_.copy(side = "sell")))(t =>
        assertM(t.buyCurrency)(
          isSubtype(
            hasField[Currency, TickerSymbol](
              "tickerSymbol",
              (a: Currency) => a.tickerSymbol,
              equalTo(Jpy)
            )
          )
        )
      )
    )
  )

  val dRateSuite = suite("#dRate")(
    testM("fails with invalid rate string")(
      checkM(transactionResponseGen.map(_.copy(rate = "hoge")))(t =>
        assertM(t.dRate.run)(fails(isSubtype[Throwable](anything)))
      )
    ),
    testM("fails with negative rate") {
      val gen = transactionResponseGen.zipWith(negativeDoubleGen)((t, r) =>
        t.copy(rate = r.toString)
      )

      checkM(gen)(t =>
        assertM(t.dRate.run)(fails(isSubtype[Throwable](anything)))
      )
    },
    testM("returns CCTraRate with valid string")(
      checkM(transactionResponseGen)(t =>
        assertM(t.dRate)(equalTo(CCTraRate.unsafeFrom(t.rate.toDouble)))
      )
    )
  )
}
