package infra.exchange

import domain.conf.Conf
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.impl.CoinCheckExchangeImpl
import zio.ZLayer

object ExchangeImpl {
  val coinCheckExchange: ZLayer[Conf, Throwable, CoincheckExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.CCAccessKey.zipWith(conf.get.CCSecretKey)(CoinCheckExchangeImpl)
    )
}
