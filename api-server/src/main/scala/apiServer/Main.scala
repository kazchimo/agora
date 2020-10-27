package apiServer

import exchange.CoinCheckExchange
import zio.console.putStrLn
import zio.{ExitCode, URIO, ZIO, ZLayer}

object Main extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = app.exitCode

  private val AccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .mapError(_ => "CC_ACCESS_KEY not found")
  private val SecretKey = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .mapError(_ => "CC_SECRET_KEY not found")

  private val coinCheckExchangeConf = ZLayer.succeedMany()

  val app = for {
    accessKey <- AccessKey
    secKey    <- SecretKey
    api        = CoinCheckExchange()
    _         <- putStrLn(accessKey)
    _         <- putStrLn(secKey)
    tra       <- api.transactions
    _         <- putStrLn(tra)
  } yield ()
}
