package infra.exchange.coincheck.responses

import domain.currency.{BitCoin, Currency, Jpy, TickerSymbol}
import domain.exchange.coincheck.CCTransaction.CCTraRate
import helpers.gen.infra.exchange.coincheck.responses.TransactionResponseGen.transactionResponseGen
import helpers.gen.std.StdGen.negativeDoubleGen
import zio.test.Assertion._
import zio.test._

object TransactionResponseTest extends DefaultRunnableSpec {
  override def spec =
    suite("TransactionResponse")(
      sellCurrencySuite,
      buyCurrencySuite,
      dRateSuite
    )

  val sellCurrencySuite =
    suite("#sellCurrency")(
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
