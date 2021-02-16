import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.WatchCoincheckTransactionUC
import zio.logging.{Logging, log}
import zio.magic._
import zio.{ExitCode, URIO, ZEnv}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = app
    .provideCustomMagicLayer(
      ConfImpl.layer,
      ExchangeImpl.coinCheckExchange,
      AsyncHttpClientZioBackend.layer(),
      Logging.console()
    )
    .exitCode

  private val app =
    log.info("start") *> WatchCoincheckTransactionUC.watch *> log.info("end")

}
