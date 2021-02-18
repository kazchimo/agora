package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCOrder
import helpers.gen.domain.exchange.coincheck.CCOrderGen.ccOrderGen
import infra.InfraError
import infra.exchange.coincheck.Endpoints
import sttp.client3.asynchttpclient.zio.SttpClientStubbing._
import sttp.client3.asynchttpclient.zio.stubbing._
import sttp.model.Method.POST
import zio.test.Assertion._
import zio.test._

trait OrdersTest { self: CoinCheckExchangeImplTest.type =>
  private val matchedWhen: StubbingWhenRequest = whenRequestMatches(r =>
    r.uri.toString() == Endpoints.orders && r.method == POST
  )
  private val successJson                      =
    "{\n  \"success\": true,\n  \"id\": 12345,\n  \"rate\": \"30010.0\",\n  \"amount\": \"1.3\",\n  \"order_type\": \"sell\",\n  \"stop_loss_rate\": null,\n  \"pair\": \"btc_jpy\",\n  \"created_at\": \"2015-01-10T05:55:38.000Z\"\n}"

  val ordersSuite = suite("#orders")(
    testM("fails if failed json returned") {
      checkM(ccOrderGen) { o =>
        val testEffect = matchedWhen.thenRespond(failJson) *> exchange.orders(o)

        assertM(testEffect.run)(
          fails(
            isSubtype[InfraError](
              hasMessage(containsString("Nonce must be incremented"))
            )
          )
        )
      }
    },
    testM("returns () if order succeed")(checkM(ccOrderGen) { o =>
      val testEffect =
        matchedWhen.thenRespond(successJson) *> exchange.orders(o)

      assertM(testEffect)(isSubtype[CCOrder](anything))
    })
  )
}
