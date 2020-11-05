package apiServer

import domain.exchange.coincheck.CoincheckExchange
import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.ZLayer
import zio.console.putStrLn

object Main extends zio.App {
  override def run(args: List[String]) =
    app.provideCustomLayer(layer).exitCode

  val layer: ZLayer[Any, Throwable, CoincheckExchange with SttpClient] =
    ConfImpl.layer >>> ExchangeImpl.coinCheckExchange ++ AsyncHttpClientZioBackend
      .layer()

  private val app =
    for {
      _      <- putStrLn("start")
      stream <-
        CoincheckExchange.publicTransactions
          .onError(e => putStrLn(e.map(_.getMessage).prettyPrint + "adfasdf"))
      _      <- stream.foreach(s => putStrLn(s))
      _      <- putStrLn("get stream")
    } yield ()
}
