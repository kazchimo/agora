package infra.exchange.coincheck.impl

import infra.exchange.coincheck.Endpoints
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.asynchttpclient.zio.stubbing._
import sttp.model.Method.GET
import sttp.ws.WebSocketFrame
import sttp.ws.testing.WebSocketStub
import zio.ZEnv
import zio.logging.Logging
import zio.stream.Stream
import zio.test.Assertion._
import zio.test.TestAspect.ignore
import zio.test._

trait PublicTransactionsTest { self: CoinCheckExchangeImplTest.type =>
  private val matchedWhen = whenRequestMatches(r =>
    r.uri.toString() == Endpoints.websocket && r.method == GET
  )
  private val layer       =
    AsyncHttpClientZioBackend.stubLayer ++ ZEnv.live ++ Logging.ignore

  protected val publicTransactionsTest: Spec[Annotations,TestFailure[Throwable],TestSuccess] = suite("#publicTransactions")(
    testM("fails if websocket closed") {
      val stub = WebSocketStub.noInitialReceive.thenRespond {
        case WebSocketFrame.Text(
              "{\"type\":\"subscribe\",\"channel\":\"btc_jpy-trades\"}",
              true,
              None
            ) => List(WebSocketFrame.text("asdf"), WebSocketFrame.text("k"))
      }

      (for {
        _   <- matchedWhen.thenRespond(stub)
        r   <- exchange.publicTransactions.fork
        res <- r.join.map(_.runCount)
      } yield assert(res)(isSubtype[Stream[Nothing, String]](anything)))
        .provideLayer(layer)
    } @@ ignore,
    test("returns websocket data as stream")(assert(1)(anything)) @@ ignore
  )
}
