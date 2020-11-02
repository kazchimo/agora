package apiServer

import domain.exchange.coincheck.{CoincheckEnv, CoincheckExchange}
import infra.exchange.coincheck.CoinCheckExchangeConfig.{
  CCEApiKey,
  CCESecretKey
}
import infra.exchange.{coincheck, ExchangeImpl}
import sttp.client3.asynchttpclient.zio.{AsyncHttpClientZioBackend, SttpClient}
import zio.console.putStrLn
import zio.{ZIO, ZLayer}

object Main extends zio.App {
  override def run(args: List[String]) =
    app.provideCustomLayer(layer).exitCode

  private val AccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .bimap(_ => "CC_ACCESS_KEY not found", CCEApiKey.applyS(_))
    .flatten
  private val SecretKey = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .bimap(_ => "CC_SECRET_KEY not found", CCESecretKey.applyS(_))
    .flatten

  private val coinCheckExchangeConf = (for {
    apiKey    <- AccessKey
    secretKey <- SecretKey
  } yield coincheck.CoinCheckExchangeConfig(apiKey, secretKey)).toLayer

  val layer: ZLayer[Any, String, CoincheckExchange with SttpClient] =
    (coinCheckExchangeConf >>> ExchangeImpl.coinCheckExchange) ++ AsyncHttpClientZioBackend
      .layer()
      .mapError(_.getMessage)

  val app: ZIO[CoincheckEnv, String, Unit] =
    for {
      _      <- putStrLn("start")
      stream <-
        CoincheckExchange.publicTransactions
          .onError(e => putStrLn(e.map(_.getMessage).prettyPrint + "adfasdf"))
          .mapError(_.toString)
      _      <- stream.foreach(s => ZIO.effectTotal(println(s)))
      _      <- putStrLn("get stream")
    } yield ()
}
