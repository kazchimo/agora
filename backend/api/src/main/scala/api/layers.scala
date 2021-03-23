package api

import domain.AppEnv
import infra.conf.ConfImpl
import infra.exchange.{ExchangeImpl, IncreasingNonceImpl}
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.logging.{LogLevel, Logging}
import zio.{ZEnv, ZLayer}

object layers {
  val all: ZLayer[ZEnv, Throwable, AppEnv] = ConfImpl.layer ++
    ExchangeImpl.coinCheckExchange ++
    ExchangeImpl.liquidExchange ++
    AsyncHttpClientZioBackend.layer() ++
    Logging.console(logLevel = LogLevel.Debug) ++
    IncreasingNonceImpl.layer(System.currentTimeMillis())
}
