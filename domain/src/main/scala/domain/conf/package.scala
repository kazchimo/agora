package domain

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory
import zio.macros.accessible
import zio.{Has, Task}

package object conf {
  type Conf = Has[Conf.Service]

  @accessible
  object Conf {
    trait Service {
      val CCAccessKey: Task[CCEAccessKey]
      val CCSecretKey: Task[CCESecretKey]
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
}
