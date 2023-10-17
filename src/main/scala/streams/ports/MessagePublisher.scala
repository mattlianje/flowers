package streams.ports

trait MessagePublisher {
  def publish(key: String, message: String): Unit
}
