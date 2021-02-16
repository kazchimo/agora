package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import zio.test.{DefaultRunnableSpec, suite}
import zio.{ Has, test }
import zio.random.Random
import zio.test.{ Annotations, Spec, TestFailure, TestSuccess }

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec with TransactionsTest with OrdersTest
    with PublicTransactionsTest {
  val exchange: CoinCheckExchangeImpl = CoinCheckExchangeImpl(
    CCEAccessKey.unsafeFrom("hoge"),
    CCESecretKey.unsafeFrom("hoge")
  )
  val failJson: String = "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  override def spec: Spec[Has[Annotations.Service] with Has[test.package.TestConfig.Service] with Has[Random.Service],TestFailure[Throwable],TestSuccess] = suite("CoinCheckExchangeImpl")(
    transactionsSuite,
    ordersSuite,
    publicTransactionsTest
  )
}
