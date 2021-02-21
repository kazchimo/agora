package lib

import _root_.zio.ZRef

package object zio {
  type UReadOnlyRef[+A]  = ZRef[Nothing, Nothing, Nothing, A]
  type UWriteOnlyRef[-A] = ZRef[Nothing, Unit, A, Nothing]
}
