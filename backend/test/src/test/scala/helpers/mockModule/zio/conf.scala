package helpers.mockModule.zio

import domain.conf._
import org.mockito.MockitoSugar._
import zio.{ULayer, ZIO, ZLayer}

object conf {
  val defaultMockConfLayer: ULayer[Conf] = ZLayer.succeed {
    val m = mock[Conf.Service]

    when(m.ccAccessKey)
      .thenReturn(ZIO.succeed(CCEAccessKey.unsafeFrom("CCEAccessKey")))
    when(m.ccSecretKey)
      .thenReturn(ZIO.succeed(CCESecretKey.unsafeFrom("CCESecretKey")))
    when(m.bfAccessKey)
      .thenReturn(ZIO.succeed(BFAccessKey.unsafeFrom("BFAccessKey")))
    when(m.bfSecretKey)
      .thenReturn(ZIO.succeed(BFSecretKey.unsafeFrom("BFSecretKey")))

    m
  }
}
