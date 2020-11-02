package domain

import zio.{Has, RIO, Task, ZIO}

package object conf {
  type Conf = Has[Conf.Service]

  object Conf {
    trait Service {
      val CCAccessKey: Task[String]
      val CCSecretKey: Task[String]
    }
  }

  def ccAccessKey: RIO[Conf, String] = ZIO.accessM(_.get.CCAccessKey)
  def ccSecretKey: RIO[Conf, String] = ZIO.accessM(_.get.CCSecretKey)
}
