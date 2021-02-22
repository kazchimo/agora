package domain.exchange

import zio.macros.accessible
import zio.{Has, UIO}

@accessible
object Nonce {
  type Nonce = Has[Service]

  trait Service {
    def getNonce: UIO[Long]
  }
}
