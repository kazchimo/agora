package lib.factory

import eu.timepit.refined.api.{Refined, Validate}
import lib.refined.refineVZE
import zio.Task

trait VOFactory[V, P] {
  type VO

  def apply(v: V Refined P): VO

  def apply(v: V)(implicit V: Validate[V, P]): Task[VO] =
    refineVZE[P, V](v).map(apply)
}
