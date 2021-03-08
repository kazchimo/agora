package domain.lib

import eu.timepit.refined.api.{Refined, Validate}
import io.estatico.newtype.Coercible
import lib.error.ClientDomainError
import lib.refined.refineVZE
import zio.IO

abstract class VOFactory[V, P] {
  type Type

  def apply(v: V Refined P): Type

  final def apply(v: V)(implicit
    V: Validate[V, P]
  ): IO[ClientDomainError, Type] = refineVZE[P, V](v).bimap(
    e => ClientDomainError(s"Failed to create ${this.toString}", Some(e)),
    apply
  )

  final def applyS(v: V)(implicit V: Validate[V, P]): IO[String, Type] =
    refineVZE[P, V](v).bimap(_.getMessage, apply)

  final def unsafeFrom(v: V): Type = apply(Refined.unsafeApply[V, P](v))
}
