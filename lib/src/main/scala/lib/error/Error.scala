package lib.error

import cats.Show
import cats.syntax.option._

abstract class Error(val msg: String, val cause: Throwable, val code: ErrorCode)
    extends Exception {
  final override def getMessage: String = msg
}

object Error {
  def unapply(e: Error): Option[(String, Throwable, ErrorCode)] =
    (e.msg, e.cause, e.code).some

  implicit def showInstance[T <: Error]: Show[T] = Show.show { t =>
    s"""
       |ErrorCode: ${t.code.toString}
       |Message: ${t.msg}
       |Cause: ${t.cause.getMessage}
       |""".stripMargin
  }
}
