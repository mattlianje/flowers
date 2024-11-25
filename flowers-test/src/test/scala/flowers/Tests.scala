package flowers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class EnigmaMachineSpec extends AnyFlatSpec with Matchers {

  val rotorI = Rotor.TypeI
  val rotorII = Rotor.TypeII
  val rotorIII = Rotor.TypeIII

  val reflectorB = Reflector.UKWB

  val plugboard = Plugboard('A' -> 'B', 'C' -> 'D')

  val enigmaMachine =
    EnigmaMachine(plugboard, List(rotorI, rotorII, rotorIII), reflectorB)

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
    val (machineAfterEncode, encodedChar) =
      enigmaMachine.encodeChar('A').run(initialMachine).value

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
    enigmaMachine.encrypt("HELLO1") shouldBe a[Failure[_]]
  }

  "EnigmaMachine.fromConfig" should "create a machine from valid JSON" in {
    val config = """
    {
      "plugboard": {
        "pairs": [["A", "B"], ["C", "D"]]
      },
      "rotors": [
        {
          "type": "TypeI",
          "position": "A",
          "ring": "A"
        },
        {
          "type": "TypeII",
          "position": "B",
          "ring": "A"
        },
        {
          "type": "TypeIII",
          "position": "C",
          "ring": "A"
        }
      ],
      "reflector": "UKWB"
    }
    """

    val machine = EnigmaMachine.fromConfig(config).get

    machine.plugboard.swap('A') shouldBe 'B'
    machine.plugboard.swap('C') shouldBe 'D'
    machine.rotors.head shouldBe a[Rotor]
    machine.rotors(0).position shouldBe 'A'
    machine.rotors(1).position shouldBe 'B'
    machine.rotors(2).position shouldBe 'C'
    machine.reflector shouldBe Reflector.UKWB
  }
}

class LorenzSpecs extends AnyFlatSpec with Matchers {

  val mu1 = Wheel.loadDefault(41)
  val mu2 = Wheel.loadDefault(31)
  val chiWheels = List.fill(5)(Wheel.loadDefault(43))
  val psiWheels = List.fill(5)(Wheel.loadDefault(43))

  val lorenzMachine = Lorenz(mu1, mu2, chiWheels, psiWheels, LetterShift)

  "LorenzMachine.encrypt" should "throw an error for invalid input" in {
    lorenzMachine.encrypt("HELLO{") shouldBe a[Failure[_]]
  }

  it should "encrypt valid input without errors" in {
    lorenzMachine.encrypt("HELLO") shouldBe a[Success[_]]
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

  "Lorenz.fromConfig" should "create a machine from valid JSON" in {
    val config = """
     {
       "mu1": {
         "pins": [1, 0, 1, 0, 1],
         "position": 0
       },
       "mu2": {
         "pins": [0, 1, 0, 1, 0],
         "position": 0
       },
       "chi": [
         {
           "pins": [1, 0, 1, 0, 1],
           "position": 0
         },
         {
           "pins": [0, 1, 0, 1, 0],
           "position": 0
         },
         {
           "pins": [1, 1, 0, 0, 1],
           "position": 0
         },
         {
           "pins": [0, 0, 1, 1, 0],
           "position": 0
         },
         {
           "pins": [1, 0, 0, 1, 1],
           "position": 0
         }
       ],
       "psi": [
         {
           "pins": [1, 1, 0, 0, 0],
           "position": 0
         },
         {
           "pins": [0, 1, 1, 0, 0],
           "position": 0
         },
         {
           "pins": [0, 0, 1, 1, 0],
           "position": 0
         },
         {
           "pins": [0, 0, 0, 1, 1],
           "position": 0
         },
         {
           "pins": [1, 0, 0, 0, 1],
           "position": 0
         }
       ],
       "shiftMode": "LetterShift"
     }
     """

    val machine = Lorenz.fromConfig(config).get

    machine.mu1.pins should have length 5
    machine.mu2.pins should have length 5
    machine.chi should have length 5
    machine.psi should have length 5
    machine.shiftMode shouldBe LetterShift

    machine.encrypt("HELLO") shouldBe a[Success[_]]
  }
}
