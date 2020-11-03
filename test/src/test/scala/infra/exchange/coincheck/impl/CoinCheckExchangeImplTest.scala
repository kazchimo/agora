package infra.exchange.coincheck.impl

import infra.exchange.coincheck.impl.TransactionsTest.transactionsSuite
import zio.test.{suite, DefaultRunnableSpec}

object CoinCheckExchangeImplTest extends DefaultRunnableSpec {
  override def spec = suite("CoinCheckExchangeImpl")(transactionsSuite)
}
