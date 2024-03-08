package commons

trait CipherMachine {
  def encrypt(data: String): Either[RuntimeException, String]
}

