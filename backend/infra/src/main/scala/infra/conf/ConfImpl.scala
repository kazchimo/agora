package infra.conf

import domain.conf._
import zio._

object ConfImpl {
  val layer: ULayer[Conf] = ZLayer.succeed(FromEnvConfImpl)
}
