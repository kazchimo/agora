package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.logging.Logging
import zio.test.{DefaultRunnableSpec, suite}

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec with TransactionsTest with OrdersTest
    with PublicTransactionsTest {
  val exchange: CoinCheckExchangeImpl = CoinCheckExchangeImpl(
    CCEAccessKey.unsafeFrom("hoge"),
    CCESecretKey.unsafeFrom("hoge")
  )
  val failJson: String                =
    "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  private val layer =
    AsyncHttpClientZioBackend.stubLayer.orDie ++ ZEnv.live ++ Logging.ignore

  override def spec = suite("CoinCheckExchangeImpl")(
    transactionsSuite,
    ordersSuite,
    publicTransactionsTest
  ).provideCustomLayer(layer)
}
