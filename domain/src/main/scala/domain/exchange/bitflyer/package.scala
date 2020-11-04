package domain.exchange

import zio.Has
import zio.macros.accessible

package object bitflyer {
  type BitflyerExchange = Has[BitflyerExchange.Service]

  @accessible
  object BitflyerExchange {
    trait Service
  }
}
