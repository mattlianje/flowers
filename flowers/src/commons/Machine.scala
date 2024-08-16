package commons

import scala.util.Try

trait Machine {
    def encrypt(input: String): Try[String]
}
