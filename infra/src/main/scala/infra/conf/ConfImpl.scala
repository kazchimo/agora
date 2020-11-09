package infra.conf

import domain.conf.{BFAccessKey, BFSecretKey, CCEAccessKey, CCESecretKey, Conf}
import infra.InfraError
import zio.{Has, Task, ULayer, ZIO, ZLayer}

object ConfImpl {
  val layer: ULayer[Has[Conf.Service]] = ZLayer.succeed {
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
      override val CCAccessKey: Task[CCEAccessKey] = ccAccessKey
      override val CCSecretKey: Task[CCESecretKey] = ccSecKey
      override val BFAccessKey: Task[BFAccessKey]  = bfAccessKey
      override val BFSecretKey: Task[BFSecretKey]  = bfSecKey
    }
  }
}
