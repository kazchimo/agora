package apiServer

import domain.exchange.coincheck.{CoincheckEnv, CoincheckExchange}
import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.console.putStrLn
import zio.{ZIO, ZLayer}

object Main extends zio.App {
  override def run(args: List[String]) =
    app.provideCustomLayer(layer).exitCode

  val layer: ZLayer[Any, Throwable, CoincheckExchange with SttpClient] =
    ConfImpl.layer >>> ExchangeImpl.coinCheckExchange ++ AsyncHttpClientZioBackend
      .layer()

  val app: ZIO[CoincheckEnv, Throwable, Unit] =
    putStrLn("start") *>
      CoincheckExchange.publicTransactions
        .onError(e => putStrLn(e.map(_.getMessage).prettyPrint + "adfasdf"))
        .map(_.foreach(s => putStrLn(s))) *>
      putStrLn("get stream")
}
