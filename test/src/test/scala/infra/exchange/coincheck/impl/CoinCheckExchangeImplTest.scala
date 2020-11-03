package infra.exchange.coincheck.impl

import domain.conf.{CCEAccessKey, CCESecretKey}
import zio.test.{suite, DefaultRunnableSpec}

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec
    with TransactionsTest {
  val exchange = CoinCheckExchangeImpl(
    CCEAccessKey.unsafeFrom("hoge"),
    CCESecretKey.unsafeFrom("hoge")
  )
  val failJson =
    "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  override def spec = suite("CoinCheckExchangeImpl")(transactionsSuite)
}
