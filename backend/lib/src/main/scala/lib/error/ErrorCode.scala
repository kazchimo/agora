package lib.error

/** The code of kinds of Errors */
sealed abstract class ErrorCode(value: String) {
  override def toString: String = value
}

/** Represents Errors being responsible for clients such as invalid parameters. */
case object ClientErr extends ErrorCode("ClientError")

/** Represents Errors being responsible Application such as Database clashes. */
case object InternalErr extends ErrorCode("InternalError")
