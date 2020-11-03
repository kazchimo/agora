package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import infra.InfraError
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.asynchttpclient.zio.stubbing._
import zio.test.Assertion._
import zio.test._

object TransactionsTest {
  val exchange = CoinCheckExchangeImpl(
    CCEAccessKey.unsafeFrom("hoge"),
    CCESecretKey.unsafeFrom("hoge")
  )

  val transactionsSuite =
    suite("#transactions")(
      testM("fails with Internal Server Error") {
        val stubEffect = whenAnyRequest.thenRespondServerError()
        val testEffect = stubEffect *> exchange.transactions

        assertM(
          testEffect.provideLayer(AsyncHttpClientZioBackend.stubLayer).run
        )(fails(isSubtype[InfraError](anything)))
      },
      testM("fails with failed response") {
        val failJson   =
          "{\"success\":false,\"error\":\"Nonce must be incremented\"}"
        val testEffect =
          whenAnyRequest.thenRespond(failJson) *>
            exchange.transactions

        assertM(
          testEffect.provideLayer(AsyncHttpClientZioBackend.stubLayer).run
        )(
          fails(
            isSubtype[InfraError](
              hasMessage(containsString("Nonce must be incremented"))
            )
          )
        )
      }
    )
}
