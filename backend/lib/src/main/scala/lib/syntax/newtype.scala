package lib.syntax
import eu.timepit.refined.api.Refined
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object newtype extends NewtypeSyntax

trait NewtypeSyntax {
  implicit final def libSyntaxRefinedNewtype[A](a: A): RefinedNewtypeOps[A] =
    new RefinedNewtypeOps(a)

  implicit final def libSyntaxNewtype[A](a: A): NewtypeOps[A] =
    new NewtypeOps(a)
}

final class RefinedNewtypeOps[A](private val a: A) extends AnyVal {
  def deepInnerV[T, P](implicit ev: Coercible[A, Refined[T, P]]): T =
    a.coerce[Refined[T, P]].value
}

final class NewtypeOps[A](private val a: A) extends AnyVal {
  def <[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): Boolean =
    ord.lt(a.coerce[B], b.coerce[B])

  def >[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): Boolean =
    ord.gt(a.coerce[B], b.coerce[B])

  def <=[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): Boolean =
    ord.lteq(a.coerce[B], b.coerce[B])

  def >=[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): Boolean =
    ord.gteq(a.coerce[B], b.coerce[B])
}
