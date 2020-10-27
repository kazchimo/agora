package exchange

import zio.ZLayer

object ExchangeImpl {
  val coinCheckExchange: ZLayer[CoinCheckExchangeConfig, Nothing, Exchange] =
    ZLayer.fromFunction(CoinCheckExchange)
}
