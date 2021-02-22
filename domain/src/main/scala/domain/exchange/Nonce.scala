package domain.exchange

import domain.AllEnv
import zio.macros.accessible
import zio.{Has, URIO}

@accessible
object Nonce {
  type Nonce = Has[Service]

  trait Service {
    def getNonce: URIO[AllEnv, Long]
  }
}
