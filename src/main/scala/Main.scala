import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.TradeInDowMethodUC
import zio.logging.{LogLevel, Logging, log}
import zio.magic._
import zio.{ExitCode, URIO, ZEnv}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = app
    .provideCustomMagicLayer(
      ConfImpl.layer,
      ExchangeImpl.coinCheckExchange,
      AsyncHttpClientZioBackend.layer(),
      Logging.console(logLevel = LogLevel.Info)
    )
    .exitCode

  private val app =
    log.info("start") *> TradeInDowMethodUC.trade(20, 5) *> log.info("end")

}
