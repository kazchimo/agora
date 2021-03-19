package lib.instance

import eu.timepit.refined.api.Refined
import io.circe.Encoder
import io.circe.refined._
import io.circe.syntax._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

object newtype extends NewtypeInstances

trait NewtypeInstances {
  implicit def newtypeRefinedEncoder[A: Coercible[
    *,
    Refined[T, P]
  ], T: Encoder, P]: Encoder[A] =
    Encoder.instance(_.coerce[Refined[T, P]].asJson)
}
