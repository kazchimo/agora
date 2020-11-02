package domain

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import lib.factory.VOFactory
import zio.{Has, RIO, Task, ZIO}

package object conf {
  type Conf = Has[Conf.Service]

  object Conf {
    trait Service {
      val CCAccessKey: Task[CCEAccessKey]
      val CCSecretKey: Task[CCESecretKey]
    }
  }

  def ccAccessKey: RIO[Conf, CCEAccessKey] = ZIO.accessM(_.get.CCAccessKey)
  def ccSecretKey: RIO[Conf, CCESecretKey] = ZIO.accessM(_.get.CCSecretKey)

  @newtype final case class CCEAccessKey(value: NonEmptyString)
  object CCEAccessKey extends VOFactory[String, NonEmpty] {
    override type VO = CCEAccessKey
  }

  @newtype final case class CCESecretKey(value: NonEmptyString)
  object CCESecretKey extends VOFactory[String, NonEmpty] {
    override type VO = CCESecretKey
  }
}
