package lib.instance

import eu.timepit.refined.api.Refined

object refined extends RefinedInstances

trait RefinedInstances {
  implicit def refinedOrdering[A: Ordering, P]: Ordering[Refined[A, P]] =
    Ordering.by(_.value)

//  implicit def refinedFractional[A, P](implicit
//    fra: Fractional[A]
//  ): Fractional[Refined[A, P]] = new Fractional[Refined[A, P]] {
//    override def div(x: Refined[A, P], y: Refined[A, P]): Refined[A, P] =
//      Refined.unsafeApply(fra.div(x.value, y.value))
//
//    override def plus(x: Refined[A, P], y: Refined[A, P]): Refined[A, P] =
//      Refined.unsafeApply(fra.plus(x.value, y.value))
//
//    override def minus(x: Refined[A, P], y: Refined[A, P]): Refined[A, P] =
//      Refined.unsafeApply(fra.minus(x.value, y.value))
//
//    override def times(x: Refined[A, P], y: Refined[A, P]): Refined[A, P] =
//      Refined.unsafeApply(fra.times(x.value, y.value))
//
//    override def negate(x: Refined[A, P]): Refined[A, P] =
//      Refined.unsafeApply(fra.negate(x.value))
//
//    override def fromInt(x: Int): Refined[A, P] =
//      Refined.unsafeApply(fra.fromInt(x))
//
//    override def parseString(str: String): Option[Refined[A, P]] =
//      fra.parseString(str).map(Refined.unsafeApply)
//
//    override def toInt(x: Refined[A, P]): Int = fra.toInt(x.value)
//
//    override def toLong(x: Refined[A, P]): Long = fra.toLong(x.value)
//
//    override def toFloat(x: Refined[A, P]): Float = fra.toFloat(x.value)
//
//    override def toDouble(x: Refined[A, P]): Double = fra.toDouble(x.value)
//
//    override def compare(x: Refined[A, P], y: Refined[A, P]): Int =
//      fra.compare(x.value, y.value)
//  }
}
