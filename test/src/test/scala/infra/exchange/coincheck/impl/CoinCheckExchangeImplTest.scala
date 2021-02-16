package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.logging.Logging
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, suite}
import zio.{ZEnv, ZLayer}

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec with TransactionsTest with OrdersTest
    with PublicTransactionsTest {
  val exchange: CoinCheckExchangeImpl = CoinCheckExchangeImpl(
    CCEAccessKey.unsafeFrom("hoge"),
    CCESecretKey.unsafeFrom("hoge")
  )
  val failJson: String                =
    "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  override def spec: ZSpec[TestEnvironment, Any] = suite(
    "CoinCheckExchangeImpl"
  )(transactionsSuite, ordersSuite, publicTransactionsTest)
}
