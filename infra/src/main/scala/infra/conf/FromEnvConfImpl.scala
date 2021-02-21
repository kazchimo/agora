package infra.conf

import domain.conf._
import infra.InfraError
import zio.{Task, ZIO}

object FromEnvConfImpl extends Conf.Service {
  private val _ccAccessKey = ZIO
    .fromOption(sys.env.get("CC_ACCESS_KEY"))
    .orElseFail(InfraError("CC_ACCESS_KEY not found"))
    .flatMap(CCEAccessKey(_))
  private val _ccSecKey    = ZIO
    .fromOption(sys.env.get("CC_SECRET_KEY"))
    .orElseFail(InfraError("CC_SECRET_KEY not found"))
    .flatMap(CCESecretKey(_))
  private val _bfAccessKey = ZIO
    .fromOption(sys.env.get("BF_ACCESS_KEY"))
    .orElseFail(InfraError("BF_ACCESS_KEY not found"))
    .flatMap(BFAccessKey(_))
  private val _bfSecKey    = ZIO
    .fromOption(sys.env.get("BF_SECRET_KEY"))
    .orElseFail(InfraError("BF_SECRET_KEY not found"))
    .flatMap(BFSecretKey(_))

  override val ccAccessKey: Task[CCEAccessKey] = _ccAccessKey
  override val ccSecretKey: Task[CCESecretKey] = _ccSecKey
  override val bfAccessKey: Task[BFAccessKey]  = _bfAccessKey
  override val bfSecretKey: Task[BFSecretKey]  = _bfSecKey
}
