package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCTransaction
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3.asynchttpclient.zio.stubbing._
import zio.test.Assertion._
import zio.test._

trait TransactionsTest { self: CoinCheckExchangeImplTest.type =>
  private val successJson =
    "{\n  \"success\": true,\n  \"transactions\": [\n    {\n      \"id\": 38,\n      \"order_id\": 49,\n      \"created_at\": \"2015-11-18T07:02:21.000Z\",\n      \"funds\": {\n        \"btc\": \"0.1\",\n        \"jpy\": \"-4096.135\"\n      },\n      \"pair\": \"btc_jpy\",\n      \"rate\": \"40900.0\",\n      \"fee_currency\": \"JPY\",\n      \"fee\": \"6.135\",\n      \"liquidity\": \"T\",\n      \"side\": \"buy\"\n    },\n    {\n      \"id\": 37,\n      \"order_id\": 48,\n      \"created_at\": \"2015-11-18T07:02:21.000Z\",\n      \"funds\": {\n        \"btc\": \"-0.1\",\n        \"jpy\": \"4094.09\"\n      },\n      \"pair\": \"btc_jpy\",\n      \"rate\": \"40900.0\",\n      \"fee_currency\": \"JPY\",\n      \"fee\": \"-4.09\",\n      \"liquidity\": \"M\",\n      \"side\": \"sell\"\n    }\n  ]\n}"

  val transactionsSuite = suite("#transactions")(
    testM("returns transactions") {
      val testEffect = whenRequestMatches(r =>
        r.uri.toString == Endpoints.transactions && r.method.toString() == "GET"
      ).thenRespond(successJson) *> exchange.transactions

      assertM(testEffect)(forall(isSubtype[CCTransaction](anything)))
    },
    testM("fails with Internal Server Error") {
      val stubEffect = whenAnyRequest.thenRespondServerError()
      val testEffect = stubEffect *> exchange.transactions

      assertM(testEffect.run)(
        fails(
          isSubtype[InfraError](
            hasMessage(containsString("Internal") && containsString("500"))
          )
        )
      )
    },
    testM("fails if failed response returned") {
      val testEffect = whenAnyRequest.thenRespond(failJson) *>
        exchange.transactions

      assertM(testEffect.run)(
        fails(
          isSubtype[InfraError](
            hasMessage(containsString("Nonce must be incremented"))
          )
        )
      )
    }
  )
}
