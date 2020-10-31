package apiServer

import domain.exchange.coincheck.CoincheckExchange
import infra.exchange.coincheck.CoinCheckExchangeConfig.{
  CCEApiKey,
  CCESecretKey
}
import infra.exchange.{coincheck, ExchangeImpl}
import zio.console.{putStrLn, Console}
import zio.{ZIO, ZLayer}

object Main extends zio.App {
  override def run(args: List[String]) = app.provideCustomLayer(layer).exitCode

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

  val layer: ZLayer[Any, String, CoincheckExchange] =
    coinCheckExchangeConf >>> ExchangeImpl.coinCheckExchange

  val app: ZIO[Console with CoincheckExchange, String, Unit] = for {
    tra <- CoincheckExchange.transactions.mapError(_.getMessage)
    _   <- putStrLn(tra.toString())
  } yield ()
}
