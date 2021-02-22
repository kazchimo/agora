package domain.exchange

import zio.{Has, UIO}

object Nonce {
  type Nonce = Has[Service]

  trait Service {
    def getNonce: UIO[Long]
  }
}
