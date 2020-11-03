package infra.exchange.coincheck.impl

import infra.InfraError
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.asynchttpclient.zio.stubbing._
import zio.test.Assertion._
import zio.test._

trait TransactionsTest { self: CoinCheckExchangeImplTest.type =>
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
