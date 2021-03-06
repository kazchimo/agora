package infra.exchange.coincheck.impl

import domain.exchange.coincheck.CoincheckExchange
import domain.exchange.liquid.LiquidExchange
import helpers.mockModule.zio.conf.defaultMockConfLayer
import infra.exchange.IncreasingNonceImpl
import org.mockito.MockitoSugar
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio._
import zio.logging.Logging
import zio.test.DefaultRunnableSpec

object CoinCheckExchangeImplTest
    extends DefaultRunnableSpec with TransactionsTest with OrdersTest
    with PublicTransactionsTest {
  val exchange: CoinCheckExchangeImpl = CoinCheckExchangeImpl()
  val failJson: String                =
    "{\"success\":false,\"error\":\"Nonce must be incremented\"}"

  private val layer = AsyncHttpClientZioBackend.stubLayer.orDie
    .++(ZEnv.live).++(Logging.ignore).++(defaultMockConfLayer).++(
      IncreasingNonceImpl.layer(0)
    ).++(ZLayer.succeed(MockitoSugar.mock[CoincheckExchange.Service])).++(
      ZLayer.succeed(MockitoSugar.mock[LiquidExchange.Service])
    )

  override def spec = suite("CoinCheckExchangeImpl")(
    transactionsSuite,
    ordersSuite,
    publicTransactionsTest
  ).provideCustomLayer(layer)
}
