package infra

final case class InfraError(message: String) extends Exception {
  override def getMessage: String = message
}
