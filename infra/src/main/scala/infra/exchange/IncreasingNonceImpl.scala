package infra.exchange

import domain.AllEnv
import domain.exchange.Nonce
import domain.exchange.Nonce.Nonce
import zio.logging.log
import zio.{UIO, ULayer, URIO, ZLayer}

final case class IncreasingNonceImpl(private var initial: Long)
    extends Nonce.Service {
  override def getNonce: URIO[AllEnv, Long] = UIO {
    initial = initial + 1
    initial
  } <* log.trace(initial.toString)
}

object IncreasingNonceImpl {
  def layer(initial: Long): ULayer[Nonce] =
    ZLayer.succeed(IncreasingNonceImpl(initial))
}
