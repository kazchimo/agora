import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import usecase.WatchCoincheckTransactionUC
import zio.console.putStrLn
import zio.logging.Logging
import zio.{ExitCode, URIO, ZEnv}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.provideCustomLayer(layer).exitCode

  private val layer = ConfImpl.layer
    .>>>(ExchangeImpl.coinCheckExchange)
    .++(AsyncHttpClientZioBackend.layer())
    .++(Logging.console())

  private val app =
    putStrLn("start") *> WatchCoincheckTransactionUC.watch *> putStrLn("end")

}
