package infra.exchange

import domain.exchange.Nonce
import domain.exchange.Nonce.Nonce
import zio.{UIO, ULayer, ZLayer}

final case class IncreasingNonceImpl(private var initial: Long)
    extends Nonce.Service {
  override def getNonce: UIO[Long] = UIO {
    initial = initial + 1
    initial
  }
}

object IncreasingNonceImpl {
  def layer(initial: Long): ULayer[Nonce] =
    ZLayer.succeed(IncreasingNonceImpl(initial))
}
