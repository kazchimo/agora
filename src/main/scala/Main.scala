import domain.exchange.coincheck.CoincheckExchange
import infra.conf.ConfImpl
import infra.exchange.ExchangeImpl
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.console.putStrLn
import zio.{ExitCode, URIO, ZEnv}

object Main extends zio.App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.provideCustomLayer(layer).exitCode

  private val layer =
    ConfImpl.layer >>> ExchangeImpl.coinCheckExchange ++ AsyncHttpClientZioBackend
      .layer()

  private val app =
    putStrLn("start") *> CoincheckExchange.transactions.map { s =>
      println(s)
    } *> putStrLn("end")

}
