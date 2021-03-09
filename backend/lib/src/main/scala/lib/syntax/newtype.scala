package lib.syntax
import eu.timepit.refined.api.Refined
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object newtype extends NewtypeSyntax

trait NewtypeSyntax {
  implicit final def libSyntaxNewtype[A](a: A): RefinedNewtypeOps[A] =
    new RefinedNewtypeOps(a)
}

final class RefinedNewtypeOps[A](private val a: A) extends AnyVal {
  def deepInnerV[T, P](implicit ev: Coercible[A, Refined[T, P]]): T =
    a.coerce[Refined[T, P]].value
}
