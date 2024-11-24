package machines.lorenz

import commons.{ShiftMode, LetterShift, FigureShift}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Try, Success, Failure}

class LorenzSpecs extends AnyFlatSpec with Matchers {

  val mu1 = Wheel.loadDefault(41)
  val mu2 = Wheel.loadDefault(31)
  val chiWheels = List.fill(5)(Wheel.loadDefault(43)) 
  val psiWheels = List.fill(5)(Wheel.loadDefault(43)) 

  val lorenzMachine = Lorenz(mu1, mu2, chiWheels, psiWheels, LetterShift)

  "LorenzMachine.encrypt" should "throw an error for invalid input" in {
    lorenzMachine.encrypt("HELLO{") shouldBe a [Failure[_]]
  }

  it should "encrypt valid input without errors" in {
    lorenzMachine.encrypt("HELLO") shouldBe a [Success[_]]
  }

  it should "encrypt and decrypt the same message correctly" in {
    val encryptedMessageTry = lorenzMachine.encrypt("HELLO")
    encryptedMessageTry match {
      case Success(encryptedMessage) =>
        val decryptedMessageTry = lorenzMachine.encrypt(encryptedMessage)
        decryptedMessageTry shouldBe Success("HELLO")
      case Failure(exception) =>
        fail(s"Encryption failed with exception: ${exception.getMessage}")
    }
  }
}
