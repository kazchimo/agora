package infra.exchange

import domain.conf.Conf
import domain.exchange.bitflyer.BitflyerExchange
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.bitflyer.impl.BitflyerExchangeImpl
import infra.exchange.coincheck.impl.CoinCheckExchangeImpl
import zio.ZLayer

object ExchangeImpl {
  val coinCheckExchange: ZLayer[Conf, Throwable, CoincheckExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.CCAccessKey.zipWith(conf.get.CCSecretKey)(CoinCheckExchangeImpl)
    )

  val bitflyerExchange: ZLayer[Conf, Throwable, BitflyerExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.BFAccessKey.zipWith(conf.get.BFSecretKey)(BitflyerExchangeImpl)
    )
}
