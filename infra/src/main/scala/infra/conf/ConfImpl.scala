package infra.conf

import domain.conf._
import infra.InfraError
import zio._

object ConfImpl {
  val layer: ULayer[Conf] = ZLayer.succeed {
    val _ccAccessKey = ZIO
      .fromOption(sys.env.get("CC_ACCESS_KEY"))
      .orElseFail(InfraError("CC_ACCESS_KEY not found"))
      .flatMap(CCEAccessKey(_))
    val _ccSecKey    = ZIO
      .fromOption(sys.env.get("CC_SECRET_KEY"))
      .orElseFail(InfraError("CC_SECRET_KEY not found"))
      .flatMap(CCESecretKey(_))
    val _bfAccessKey = ZIO
      .fromOption(sys.env.get("BF_ACCESS_KEY"))
      .orElseFail(InfraError("BF_ACCESS_KEY not found"))
      .flatMap(BFAccessKey(_))
    val _bfSecKey    = ZIO
      .fromOption(sys.env.get("BF_SECRET_KEY"))
      .orElseFail(InfraError("BF_SECRET_KEY not found"))
      .flatMap(BFSecretKey(_))

    new Conf.Service {
      override val ccAccessKey: Task[CCEAccessKey] = _ccAccessKey
      override val ccSecretKey: Task[CCESecretKey] = _ccSecKey
      override val bfAccessKey: Task[BFAccessKey]  = _bfAccessKey
      override val bfSecretKey: Task[BFSecretKey]  = _bfSecKey
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
