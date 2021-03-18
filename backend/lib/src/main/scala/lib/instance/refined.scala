package lib.instance

import lib.refined.NonNegativeDouble

object refined extends RefinedInstances

trait RefinedInstances {
  implicit val nonNegativeDoubleOrdering: Ordering[NonNegativeDouble] =
    Ordering.by(_.value)
}
