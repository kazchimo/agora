package infra.exchange.coincheck.impl

import helpers.gen.domain.exchange.coincheck.CCOrderGen.ccOrderGen
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.asynchttpclient.zio.stubbing._
import sttp.model.Method.POST
import zio.test.Assertion._
import zio.test._
import zio._

trait OrdersTest { self: CoinCheckExchangeImplTest.type =>
  val matchedWehn = whenRequestMatches(r =>
    r.uri.toString() == Endpoints.orders && r.method == POST
  )

  val ordersSuite =
    suite("#orders")(testM("fails with failed json") {
      checkM(ccOrderGen) { o =>
        val testEffect = matchedWehn.thenRespond(failJson) *> exchange.orders(o)

        assertM(
          testEffect
            .provideLayer(AsyncHttpClientZioBackend.stubLayer ++ ZEnv.live)
            .run
        )(
          fails(
            isSubtype[InfraError](
              hasMessage(containsString("Nonce must be incremented"))
            )
          )
        )
      }
    })
}
