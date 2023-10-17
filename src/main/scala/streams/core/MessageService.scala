package streams.core

import streams.ports.MessagePublisher

class MessageService(publisher: MessagePublisher) {
  def send(key: String, message: String): Unit = {
    // TODO

    publisher.publish(key, message)

    // TODO
  }
}

