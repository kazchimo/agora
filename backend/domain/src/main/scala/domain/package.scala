import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CoincheckExchange
import domain.exchange.liquid.LiquidExchange
import sttp.client3.asynchttpclient.zio.SttpClient
import zio.ZEnv
import zio.logging.Logging

package object domain {
  type AllEnv = ZEnv
    with Conf with CoincheckExchange with LiquidExchange with Logging with Nonce
    with SttpClient
}
