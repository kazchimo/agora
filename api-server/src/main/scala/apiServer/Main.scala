package apiServer

import eu.timepit.refined._
import eu.timepit.refined.predicates.all.NonEmpty
import exchange.CoinCheckExchangeConfig.{CCEApiKey, CCESecretKey}
import exchange.{CoinCheckExchange, CoinCheckExchangeConfig}
import zio.{Has, ZIO}
import zio.console.{Console, putStrLn}

object Main extends zio.App {
  override def run(args: List[String]) = app.provideCustomLayer(coinCheckExchangeConf).exitCode

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

  val app: ZIO[Console with Has[CoinCheckExchangeConfig], String, Unit] = for {
    accessKey <- AccessKey
    secKey    <- SecretKey
    api        = CoinCheckExchange()
    _         <- putStrLn(accessKey)
    _         <- putStrLn(secKey)
    tra       <- api.transactions
    _         <- putStrLn(tra)
  } yield ()
}
