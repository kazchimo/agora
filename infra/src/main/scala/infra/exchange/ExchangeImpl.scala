package infra.exchange

import domain.conf.Conf
import domain.exchange.bitflyer.BitflyerExchange
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.bitflyer.impl.BitflyerExchangeImpl
import infra.exchange.coincheck.impl.{
  CoinCheckExchangeImpl,
  DryWriteCoincheckExchangeImpl
}
import zio.{ULayer, ZLayer}

object ExchangeImpl {
  val coinCheckExchange: ZLayer[Conf, Throwable, CoincheckExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.ccAccessKey.zipWith(conf.get.ccSecretKey)(CoinCheckExchangeImpl)
    )

  def dryWriteCoinCheckExchange(
    orderSettledInterval: Int
  ): ULayer[CoincheckExchange] =
    ZLayer.succeed(DryWriteCoincheckExchangeImpl(orderSettledInterval))

  val bitflyerExchange: ZLayer[Conf, Throwable, BitflyerExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.bfAccessKey.zipWith(conf.get.bfSecretKey)(BitflyerExchangeImpl)
    )
}
