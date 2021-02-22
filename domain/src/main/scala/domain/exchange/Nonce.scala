package domain.exchange

import zio.macros.accessible
import zio.{Has, URIO}

@accessible
object Nonce {
  type Nonce = Has[Service]

  import domain.AllEnv
  trait Service {
    def getNonce: URIO[AllEnv, Long]
  }
}
