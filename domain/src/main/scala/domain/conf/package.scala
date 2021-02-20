package domain

import domain.lib.VOFactory
import eu.timepit.refined.collection.NonEmpty
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
    }
  }

  @newtype final case class CCEAccessKey(value: NonEmptyString)
  object CCEAccessKey extends VOFactory[String, NonEmpty] {
    override type VO = CCEAccessKey
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey extends VOFactory[String, NonEmpty] {
    override type VO = CCESecretKey
  }

  @newtype final case class BFAccessKey(value: NonEmptyString)
  object BFAccessKey extends VOFactory[String, NonEmpty] {
    override type VO = BFAccessKey
  }

  @newtype final case class BFSecretKey(value: NonEmptyString)
  object BFSecretKey extends VOFactory[String, NonEmpty] {
    override type VO = BFSecretKey
  }
}
