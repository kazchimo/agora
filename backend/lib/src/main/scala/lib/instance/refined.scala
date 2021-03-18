package lib.instance

import eu.timepit.refined.api.Refined

object refined extends RefinedInstances

trait RefinedInstances {
  implicit def refinedOrdering[A: Ordering, P]: Ordering[Refined[A, P]] =
    Ordering.by(_.value)
}
