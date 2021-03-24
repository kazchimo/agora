package lib.syntax
import eu.timepit.refined.api.{Refined, Validate}
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import lib.refined.refineVZE
import zio.ZIO

import newtype._

object newtype extends NewtypeSyntax

trait NewtypeSyntax {
  implicit final def libSyntaxRefinedNewtype[T, P, A: Coercible[
    *,
    Refined[T, P]
  ]](a: A): RefinedNewtypeOps[T, P, A] = new RefinedNewtypeOps(a)

  implicit final def libSyntaxNewtype[A](a: A): NewtypeOps[A] =
    new NewtypeOps(a)
}

final class RefinedNewtypeOps[T, P, A: Coercible[*, Refined[T, P]]](
  private val a: A
) {
  def deepInnerV: T = a.coerce[Refined[T, P]].value

  def zplus(b: A)(implicit
    ev: Coercible[Refined[T, P], A],
    num: Numeric[T],
    v: Validate[T, P]
  ): ZIO[Any, Throwable, A] = refineVZE[P, T](
    num.plus(a.coerce[Refined[T, P]].value, b.coerce[Refined[T, P]].value)
  ).map(_.coerce[A])

  def zminus(b: A)(implicit
    ev: Coercible[Refined[T, P], A],
    num: Numeric[T],
    v: Validate[T, P]
  ): ZIO[Any, Throwable, A] = refineVZE[P, T](
    num.minus(a.coerce[Refined[T, P]].value, b.coerce[Refined[T, P]].value)
  ).map(_.coerce[A])
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

  def min[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): A =
    if (a >= b) b else a

  def max[B](b: A)(implicit coe: Coercible[A, B], ord: Ordering[B]): A =
    if (a <= b) b else a
}
