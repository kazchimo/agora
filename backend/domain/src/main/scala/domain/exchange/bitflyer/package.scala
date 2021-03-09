package domain.exchange

import sttp.client3.asynchttpclient.zio.SttpClient
import zio.console.Console
import zio.macros.accessible
import zio.{Has, RIO}

package object bitflyer {
  type BitflyerExchange = Has[BitflyerExchange.Service]

  @accessible
  object BitflyerExchange {
    trait Service {
      def childOrder(order: BFChildOrder): RIO[SttpClient with Console, Unit]
    }
  }
}
