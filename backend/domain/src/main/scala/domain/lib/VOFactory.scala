package domain.lib

import eu.timepit.refined.api.{Refined, Validate}
import lib.error.ClientDomainError
import lib.refined.refineVZE
import zio.IO

abstract class VOFactory {
  type Type
  type Repr <: Refined[_, _]

  def apply(v: Repr): Type

  final def apply[V, P](v: V)(implicit
    ev: Refined[V, P] =:= Repr,
    V: Validate[V, P]
  ): IO[ClientDomainError, Type] = refineVZE[P, V](v).bimap(
    e => ClientDomainError(s"Failed to create ${this.toString}", Some(e)),
    a => apply(ev(a))
  )

  final def applyS[V, P](
    v: V
  )(implicit ev: Refined[V, P] =:= Repr, V: Validate[V, P]): IO[String, Type] =
    refineVZE[P, V](v).bimap(_.getMessage, a => apply(ev(a)))

  final def unsafeFrom[V, P](v: V)(implicit ev: Refined[V, P] =:= Repr): Type =
    apply(Refined.unsafeApply[V, P](v))
}
