package lib

import _root_.sttp.client3._
import _root_.sttp.model.MediaType._

package object sttp {
  val jsonRequest: RequestT[Empty, Either[String, String], Any] =
    basicRequest.contentType(ApplicationJson)
}
