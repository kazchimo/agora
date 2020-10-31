package infra.exchange

import domain.exchange.coincheck.Exchange
import infra.exchange.coincheck.{CoinCheckExchange, CoinCheckExchangeConfig}
import zio.{Has, ZLayer}

object ExchangeImpl {
  val coinCheckExchange
    : ZLayer[Has[CoinCheckExchangeConfig], Nothing, Exchange] =
    ZLayer.fromFunction(conf => CoinCheckExchange(conf.get))
}
