package apiServer

import domain.exchange.bitflyer.BFChildOrder.{BFOrderPrice, BFOrderSize}
import domain.exchange.bitflyer.{
  BFBtcJpy,
  BFBuy,
  BFChildOrder,
  BFGoodTilCanceledQCE,
  BFLimitOrderType,
  BFQuantityConditionsEnforcement,
  BitflyerExchange
}
import domain.exchange.coincheck.CoincheckExchange
import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.ZLayer
import zio.console.putStrLn

object Main extends zio.App {
  override def run(args: List[String]) =
    app.provideCustomLayer(layer).exitCode

  val layer: ZLayer[Any, Throwable, BitflyerExchange with SttpClient] =
    ConfImpl.layer >>> ExchangeImpl.bitflyerExchange ++ AsyncHttpClientZioBackend
      .layer()

  private val app =
    for {
      _ <- putStrLn("start")
      _ <-
        BitflyerExchange
          .childOrder(
            BFChildOrder(
              BFLimitOrderType,
              BFBuy,
              BFOrderPrice.unsafeFrom(3),
              BFOrderSize.unsafeFrom(4),
              BFGoodTilCanceledQCE,
              BFBtcJpy
            )
          )
      _ <- putStrLn("end")
    } yield ()
}
