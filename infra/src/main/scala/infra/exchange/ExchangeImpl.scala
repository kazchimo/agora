package infra.exchange

import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.{CoinCheckExchangeConfig, CoinCheckExchangeImpl}
import zio.{Has, ZLayer}

object ExchangeImpl {
  val coinCheckExchange
    : ZLayer[Has[CoinCheckExchangeConfig], Nothing, CoincheckExchange] =
    ZLayer.fromFunction(conf => CoinCheckExchangeImpl(conf.get))
}
