package machines.enigma


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Try, Success, Failure}
import cats.data.State
import commons._

class EnigmaMachineSpec extends AnyFlatSpec with Matchers {

  val rotorI = Rotor.TypeI
  val rotorII = Rotor.TypeII
  val rotorIII = Rotor.TypeIII

  val reflectorB = Reflector.UKWB

  val plugboard = Plugboard('A' -> 'B', 'C' -> 'D')

  val enigmaMachine = EnigmaMachine(plugboard, List(rotorI, rotorII, rotorIII), reflectorB)

  "Rotor" should "encode characters correctly in forward direction" in {
    rotorI.encode('A') shouldBe 'U'
    rotorI.encode('B') shouldBe 'W'
    rotorII.encode('A') shouldBe 'A'
  }

  it should "encode characters correctly in reverse direction" in {
    rotorI.encode('E', reverse = true) shouldBe 'L'
    rotorI.encode('K', reverse = true) shouldBe 'N'
    rotorII.encode('A', reverse = true) shouldBe 'A'
  }

  it should "rotate correctly" in {
    val rotatedRotor = rotorI.rotate
    rotatedRotor.position shouldBe 'B'
  }

  "Reflector" should "reflect characters correctly" in {
    reflectorB.reflect('A') shouldBe 'Y'
    reflectorB.reflect('Y') shouldBe 'A'
    reflectorB.reflect('B') shouldBe 'R'
  }

  "Plugboard" should "swap characters correctly" in {
    plugboard.swap('A') shouldBe 'B'
    plugboard.swap('B') shouldBe 'A'
    plugboard.swap('C') shouldBe 'D'
    plugboard.swap('D') shouldBe 'C'
    plugboard.swap('E') shouldBe 'E'
  }

  "EnigmaMachine" should "encode a single character correctly" in {
    val initialMachine = enigmaMachine
    val (machineAfterEncode, encodedChar) = enigmaMachine.encodeChar('A').run(initialMachine).value

    encodedChar shouldBe 'H'
    machineAfterEncode.rotors.head.position shouldBe 'B' 
  }

  it should "encode a string correctly" in {
    val input = "HELLO"
    val encodedString = enigmaMachine.encrypt(input)
    
    encodedString shouldBe Success("AHUMR")
  }

  it should "validate input correctly" in {
    EnigmaMachine.validateInput("HELLO") shouldBe Some("HELLO")
    EnigmaMachine.validateInput("HELLO1") shouldBe None 
    EnigmaMachine.validateInput("HELLO!") shouldBe None 
  }

  it should "throw an error for invalid input" in {
    enigmaMachine.encrypt("HELLO1") shouldBe a [Failure[_]]
  }
}