package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CCPublicTransaction
import domain.exchange.coincheck.CCPublicTransaction._
import infra.exchange.coincheck.Endpoints
import lib.error.InternalInfraError
import sttp.client3.asynchttpclient.zio.SttpClientStubbing._
import sttp.client3.asynchttpclient.zio.stubbing._
import sttp.model.Method.GET
import sttp.ws.WebSocketFrame
import sttp.ws.testing.WebSocketStub
import zio.stream.Stream
import zio.test.Assertion._
import zio.test.TestAspect.ignore
import zio.test._

trait PublicTransactionsTest { self: CoinCheckExchangeImplTest.type =>
  private val matchedWhen: StubbingWhenRequest = whenRequestMatches(r =>
    r.uri.toString() == Endpoints.websocket && r.method == GET
  )

  protected val publicTransactionsTest = suite("#publicTransactions")(
    testM("fails if websocket closed") {
      val stub = WebSocketStub.noInitialReceive.thenRespond {
        case WebSocketFrame.Text(
              "{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}",
              true,
              None
            ) => List(WebSocketFrame.text("asdf"), WebSocketFrame.text("k"))
      }

      for {
        _   <- matchedWhen.thenRespond(stub)
        r   <- exchange.publicTransactions.fork
        res <- r.join.map(_.runCount)
      } yield assert(res)(isSubtype[Stream[Nothing, String]](anything))
    } @@ ignore,
    test("returns websocket data as stream")(assert(1)(anything)) @@ ignore,
    suite("#textToModel")(
      testM("convert text to transaction model") {
        val text = "[2357068,\"btc_jpy\",\"148642.0\",\"0.7828\",\"buy\"]"
        assertM(PublicTransactions.textToModel(text))(
          equalTo(
            CCPublicTransaction(
              CCPubTraId.unsafeFrom(2357068),
              CCPubTraPair.unsafeFrom("btc_jpy"),
              CCPubTraRate.unsafeFrom(148642),
              CCPubTraQuantity.unsafeFrom(0.7828),
              CCPubTraSide.CCPubTraBuy
            )
          )
        )
      },
      testM("fail with InfraError if invalid string") {
        checkM(Gen.anyString)(s =>
          assertM(PublicTransactions.textToModel(s).run)(
            fails(isSubtype[InternalInfraError](anything))
          )
        )
      }
    )
  )
}
