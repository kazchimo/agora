package infra.exchange

import domain.conf.Conf
import domain.exchange.bitflyer.BitflyerExchange
import domain.exchange.coincheck.CCOrder.CCOrderRequestRate
import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.bitflyer.impl.BitflyerExchangeImpl
import infra.exchange.coincheck.impl.{
  CoinCheckExchangeImpl,
  DryWriteCoincheckExchangeImpl
}
import zio.{ULayer, ZLayer}

object ExchangeImpl {
  val coinCheckExchange: ZLayer[Any, Throwable, CoincheckExchange] =
    ZLayer.succeed(CoinCheckExchangeImpl())

  def dryWriteCoinCheckExchange(
    orderSettledInterval: Int,
    marketRate: CCOrderRequestRate
  ): ULayer[CoincheckExchange] = ZLayer.succeed(
    DryWriteCoincheckExchangeImpl(orderSettledInterval, marketRate)
  )

  val bitflyerExchange: ZLayer[Conf, Throwable, BitflyerExchange] =
    ZLayer.fromFunctionM(conf =>
      conf.get.bfAccessKey.zipWith(conf.get.bfSecretKey)(BitflyerExchangeImpl)
    )
}
