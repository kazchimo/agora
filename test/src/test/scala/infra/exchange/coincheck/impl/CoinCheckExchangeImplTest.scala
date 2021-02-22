package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CoincheckExchange
import infra.conf.ConfImpl
import infra.exchange.{ExchangeImpl, IncreasingNonceImpl}
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.logging.Logging
import zio.test.DefaultRunnableSpec
import zio.magic._

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec with TransactionsTest with OrdersTest
    with PublicTransactionsTest {
  val exchange: CoinCheckExchangeImpl = CoinCheckExchangeImpl()
  val failJson: String                =
    "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  private val layer =
    AsyncHttpClientZioBackend.stubLayer.orDie ++ ZEnv.live ++ Logging.ignore ++ ConfImpl.stubLayer ++ IncreasingNonceImpl
      .layer(0) ++ CoincheckExchange.stubLayer()

  override def spec = suite("CoinCheckExchangeImpl")(
    transactionsSuite,
    ordersSuite,
    publicTransactionsTest
  ).provideCustomLayer(layer)
}
