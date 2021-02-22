package domain.exchange

import zio.{Has, Task}

object Nonce {
  type Nonce = Has[Service]

  trait Service {
    def getNonce: Task[Long]
  }
}
