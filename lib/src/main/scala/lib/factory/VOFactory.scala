package lib.factory

import cats.syntax.eq._
import eu.timepit.refined.api.{Refined, Validate}
import lib.refined.refineVZE
import zio.{IO, Task, ZIO}

trait VOFactory[V, P] {
  type VO

  def apply(v: V Refined P): VO

  def apply(v: V)(implicit V: Validate[V, P]): Task[VO] =
    refineVZE[P, V](v).map(apply)

  def applyS(v: V)(implicit V: Validate[V, P]): IO[String, VO] =
    refineVZE[P, V](v).map(apply).mapError(_.getMessage)

  def unsafeFrom(v: V): VO = apply(Refined.unsafeApply[V, P](v))
}

trait SumVOFactory {
  type VO
  val sums: Seq[VO]
  def extractValue(v: VO): String

  def apply(v: String): Task[VO] = {
    val fac = sums.foldLeft(PartialFunction.empty[String, VO]) {
      case (prev, elem) =>
        prev.orElse {
          case key if key === extractValue(elem) => elem
        }
    }

    if (fac.isDefinedAt(v)) ZIO.succeed(fac(v))
    else ZIO.fail(new Exception(s"factory is not defined at $v "))
  }
}
