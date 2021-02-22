import domain.conf.Conf
import domain.exchange.Nonce.Nonce
import domain.exchange.coincheck.CoincheckExchange
import zio.ZEnv
import zio.logging.Logging

package object domain {
  type AllEnv = ZEnv with Conf with CoincheckExchange with Logging with Nonce
}
