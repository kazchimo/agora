package infra.conf

import domain.conf._
import infra.InfraError
import zio._

object ConfImpl {
  val layer: ULayer[Conf] = ZLayer.succeed {
    val ccAccessKey = ZIO
      .fromOption(sys.env.get("CC_ACCESS_KEY"))
      .orElseFail(InfraError("CC_ACCESS_KEY not found"))
      .flatMap(CCEAccessKey(_))
    val ccSecKey    = ZIO
      .fromOption(sys.env.get("CC_SECRET_KEY"))
      .orElseFail(InfraError("CC_SECRET_KEY not found"))
      .flatMap(CCESecretKey(_))
    val bfAccessKey = ZIO
      .fromOption(sys.env.get("BF_ACCESS_KEY"))
      .orElseFail(InfraError("BF_ACCESS_KEY not found"))
      .flatMap(BFAccessKey(_))
    val bfSecKey    = ZIO
      .fromOption(sys.env.get("BF_SECRET_KEY"))
      .orElseFail(InfraError("BF_SECRET_KEY not found"))
      .flatMap(BFSecretKey(_))

    new Conf.Service {
      override val ccAccessKey: Task[CCEAccessKey] = ccAccessKey
      override val ccSecretKey: Task[CCESecretKey] = ccSecKey
      override val bfAccessKey: Task[BFAccessKey]  = bfAccessKey
      override val bfSecretKey: Task[BFSecretKey]  = bfSecKey
    }
  }

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
