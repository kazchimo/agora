package infra.conf

import domain.conf.{CCEAccessKey, CCESecretKey, Conf}
import infra.InfraError
import zio.{Has, Task, ULayer, ZIO, ZLayer}

object ConfImpl {
  val layer: ULayer[Has[Conf.Service]] = ZLayer.succeed {
    val accessKey = ZIO
      .fromOption(sys.env.get("CC_ACCESS_KEY"))
      .mapError(_ => InfraError("CC_ACCESS_KEY not found"))
      .flatMap(CCEAccessKey(_))
    val secKey    = ZIO
      .fromOption(sys.env.get("CC_SECRET_KEY"))
      .mapError(_ => InfraError("CC_SECRET_KEY not found"))
      .flatMap(CCESecretKey(_))

    new Conf.Service {
      override val CCAccessKey: Task[CCEAccessKey] = accessKey
      override val CCSecretKey: Task[CCESecretKey] = secKey
    }
  }
}
