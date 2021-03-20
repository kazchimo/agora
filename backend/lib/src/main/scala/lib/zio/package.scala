package lib

import _root_.zio.ZRef
import _root_.zio.stream._

package object zio {
  type UReadOnlyRef[+A]  = ZRef[Nothing, Nothing, Nothing, A]
  type UWriteOnlyRef[-A] = ZRef[Nothing, Unit, A, Nothing]

  type EStream[+A] = Stream[Throwable, A]
}
