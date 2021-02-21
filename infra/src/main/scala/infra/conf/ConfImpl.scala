package infra.conf

import domain.conf._
import zio._

object ConfImpl {
  val layer: ULayer[Conf] = ZLayer.succeed(FromEnvConfImpl)

  val stubLayer: ULayer[Conf] = ZLayer.succeed(new Conf.Service {
    override val ccAccessKey: Task[CCEAccessKey] =
      ZIO.succeed(CCEAccessKey.unsafeFrom("CCEAccessKey"))
    override val ccSecretKey: Task[CCESecretKey] =
      ZIO.succeed(CCESecretKey.unsafeFrom("CCESecretKey"))
    override val bfAccessKey: Task[BFAccessKey]  =
      ZIO.succeed(BFAccessKey.unsafeFrom("CCEAccessKey"))
    override val bfSecretKey: Task[BFSecretKey]  =
      ZIO.succeed(BFSecretKey.unsafeFrom("CCEAccessKey"))
  })
}
