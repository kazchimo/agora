package lib

import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.refineV
import zio.{IO, Task, ZIO}

package object refined {
  def refineVZ[P, T](t: T)(implicit
    v: Validate[T, P]
  ): IO[String, Refined[T, P]] = ZIO.fromEither(refineV[P](t))

  def refineVZE[P, T](t: T)(implicit v: Validate[T, P]): Task[Refined[T, P]] =
    ZIO.effect(refineV[P].unsafeFrom(t))

  type NonNegativeDouble = Refined[Double, NonNegative]
}
