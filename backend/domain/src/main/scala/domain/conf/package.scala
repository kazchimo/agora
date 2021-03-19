package domain

import domain.lib.VOFactory
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import zio.macros.accessible
import zio.{Has, Task}

package object conf {
  type Conf = Has[Conf.Service]

  @accessible
  object Conf {
    trait Service {
      val ccAccessKey: Task[CCEAccessKey]
      val ccSecretKey: Task[CCESecretKey]
      val bfAccessKey: Task[BFAccessKey]
      val bfSecretKey: Task[BFSecretKey]
      val liquidTokenId: Task[LiquidTokenId]
      val liquidSecret: Task[LiquidSecret]
    }
  }

  @newtype final case class CCEAccessKey(value: NonEmptyString)
  object CCEAccessKey extends VOFactory

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey extends VOFactory

  @newtype final case class BFAccessKey(value: NonEmptyString)
  object BFAccessKey extends VOFactory

  @newtype final case class BFSecretKey(value: NonEmptyString)
  object BFSecretKey extends VOFactory

  @newtype final case class LiquidTokenId(value: NonEmptyString)
  object LiquidTokenId extends VOFactory

  @newtype final case class LiquidSecret(value: NonEmptyString)
  object LiquidSecret extends VOFactory
}
