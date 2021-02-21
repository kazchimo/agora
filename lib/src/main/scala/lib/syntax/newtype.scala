package lib.syntax
import eu.timepit.refined.api.Refined
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import shapeless.Unwrapped
import shapeless.syntax.unwrapped._

object newtype extends NewtypeSyntax

trait NewtypeSyntax {
  implicit final def libSyntaxNewtype[A](a: A): RefinedNewtypeOps[A] =
    new RefinedNewtypeOps(a)
}

final class RefinedNewtypeOps[A](private val a: A) extends AnyVal {
  def deepInnerV[T, P, X](implicit
    ev: Coercible[A, Refined[T, P]],
    uw: Unwrapped.Aux[Refined[T, P], X]
  ): X = a.coerce[Refined[T, P]].unwrap
}
