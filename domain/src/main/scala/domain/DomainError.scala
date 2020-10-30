package domain

final case class DomainError(message: String) extends Exception {
  override def getMessage: String = message
}
