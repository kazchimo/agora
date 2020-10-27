package apiServer

import eu.timepit.refined._
import eu.timepit.refined.predicates.all.NonEmpty
import exchange.CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import exchange.{CoinCheckExchangeConfig, Exchange, ExchangeImpl}
import zio.{ZIO, ZLayer}
import zio.console.{Console, putStrLn}

object Main extends zio.App {
  override def run(args: List[String]) = app.provideCustomLayer(layer).exitCode

  private val AccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .mapError(_ => "CC_ACCESS_KEY not found")
  private val SecretKey = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .mapError(_ => "CC_SECRET_KEY not found")

  private val coinCheckExchangeConf = (for {
    apiKey       <- AccessKey
    refApiKey    <- ZIO.fromEither(refineV[NonEmpty](apiKey))
    secretKey    <- SecretKey
    refSecretKey <- ZIO.fromEither(refineV[NonEmpty](secretKey))
  } yield CoinCheckExchangeConfig(
    CCEApiKey(refApiKey),
    CCESecretKey(refSecretKey)
  )).toLayer

  val layer: ZLayer[Any, String, Exchange] = coinCheckExchangeConf >>> ExchangeImpl.coinCheckExchange

  val app: ZIO[Console with Exchange, String, Unit] = for {
    accessKey <- AccessKey
    secKey    <- SecretKey
    _         <- putStrLn(accessKey)
    _         <- putStrLn(secKey)
    tra       <- Exchange.transactions
    _         <- putStrLn(tra)
  } yield ()
}
