package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, suite}

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
