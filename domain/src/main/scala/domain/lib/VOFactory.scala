package domain.lib

import eu.timepit.refined.api.{Refined, Validate}
import lib.error.ClientDomainError
import lib.refined.refineVZE
import zio.IO

trait VOFactory[V, P] {
  type VO

  def apply(v: V Refined P): VO

  final def apply(v: V)(implicit V: Validate[V, P]): IO[ClientDomainError, VO] =
    refineVZE[P, V](v)
      .bimap(e => ClientDomainError("Failed to create VO", Some(e)), apply)

  final def applyS(v: V)(implicit V: Validate[V, P]): IO[String, VO] =
    refineVZE[P, V](v).bimap(_.getMessage, apply)

  final def unsafeFrom(v: V): VO = apply(Refined.unsafeApply[V, P](v))
}
