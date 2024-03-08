package lorenz

import commons.Baudot.BitString
import commons.Language.{English, German}
import commons.PlaintextReader.{loadLorenzBitStream, loadLorenzPlaintext}
import machines.lorenz.{LorenzMachine, Wheel}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class LorenzMachineSpecs extends AnyFlatSpec with Matchers {

  private val TEST_MACHINE =
    LorenzMachine(Wheel(List(0, 1), 0), Wheel(List(1, 0), 0), List(), List())
  private val TEST_MACHINE_2 = LorenzMachine.getDefault()
  private val BITSTRINGS_ZITADELLE: Seq[BitString] = loadLorenzBitStream(
    German
  ) match {
    case Some(value) => value
    case None        => Seq.empty[BitString]
  }

  "A Wheel" should "rotate correctly" in {
    val wheel = Wheel.loadDefaultWheel(4) // A wheel with pins 0,1,0,1
    wheel.rotate.pos shouldBe 1
  }

  "A LorenzMachine" should "rotate Mu1 correctly" in {
    val machine = TEST_MACHINE
    machine.updateState().mu1.pos shouldBe 1
  }

  it should "rotate Mu2 conditionally" in {
    val machine = TEST_MACHINE
    machine
      .updateState()
      .mu2
      .pos shouldBe 0 // Mu2 should not rotate when Mu1's bit is 0
    val newMachine =
      TEST_MACHINE.updateState() // Mu2 should rotate when Mu1's bit is 1

    newMachine.updateState().mu2.pos shouldBe 1
  }

  it should "encipher correctly" in {
    val machine = LorenzMachine
      .createMachine()
      .getOrElse(fail("Failed to create LorenzMachine"))

    val input = "Abteilung"
    val expectedOutput = "AIKVIFOWG"

    val result = machine.encrypt(input)

    result shouldBe Right(expectedOutput)
  }

  it should "encipher a big message correctly" in {
    val machine = LorenzMachine
      .createMachine()
      .getOrElse(fail("Failed to create LorenzMachine"))

    val input = loadLorenzPlaintext(German)

    val result = machine.encrypt(input)
    // Checks if results is Right()
    result should be a Symbol("right")
  }

  it should "encipher BITSTRINGS_ZITADELLE correctly" in {
    val machine = LorenzMachine
      .createMachine()
      .getOrElse(fail("Failed to create LorenzMachine"))

    // Encipher the BITSTRINGS_ZITADELLE input
    val result = machine.encipher(BITSTRINGS_ZITADELLE)

    // Check if result is Right() and matches expected
    result match {
      case Right(output) =>
        println("SUCCESS")
      case Left(error) => fail(s"Enciphering failed with error: $error")
    }
  }

  "A LorenzMachine" should "always rotate the mu1 wheel upon updateState" in {
    val updatedMachine = TEST_MACHINE_2.updateState()

    updatedMachine.mu1.pos should not be TEST_MACHINE_2.mu1.pos
  }

  it should "rotate mu2 wheel if mu1's bit is 1" in {
    val updatedMachine = TEST_MACHINE_2.updateState()

    if (TEST_MACHINE_2.mu1.bit == 1)
      updatedMachine.mu2.pos should not be TEST_MACHINE_2.mu2.pos
    else updatedMachine.mu2.pos should be(TEST_MACHINE_2.mu2.pos)
  }

  it should "always rotate the chi wheels upon updateState" in {
    val updatedMachine = TEST_MACHINE_2.updateState()

    updatedMachine.chi.zip(TEST_MACHINE_2.chi).foreach {
      case (updatedWheel, initialWheel) =>
        updatedWheel.pos should not be initialWheel.pos
    }
  }

  "A LorenzMachine" should "correctly decrypt enciphered text back to the original plaintext" in {
    val machine = LorenzMachine
      .createMachine()
      .getOrElse(fail("Failed to create LorenzMachine"))

    val plaintext = "ABTEILUNGXYZ"
    val enciphered = machine.encrypt(plaintext) match {
      case Right(ciphertext) => ciphertext
      case Left(error)       => fail(s"Enciphering failed with error: $error")
    }

    val deciphered = machine.encrypt(enciphered) match {
      case Right(originalText) => originalText
      case Left(error)         => fail(s"Deciphering failed with error: $error")
    }
    deciphered shouldBe plaintext
  }

  it should "rotate psi wheels if mu1 and mu2 bits XOR to 1" in {
    val updatedMachine = TEST_MACHINE_2.updateState()

    if ((TEST_MACHINE_2.mu1.bit ^ TEST_MACHINE_2.mu2.bit) == 1) {
      updatedMachine.psi.zip(TEST_MACHINE_2.psi).foreach {
        case (updatedWheel, initialWheel) =>
          updatedWheel.pos should not be initialWheel.pos
      }
    } else {
      updatedMachine.psi.zip(TEST_MACHINE_2.psi).foreach {
        case (updatedWheel, initialWheel) =>
          updatedWheel.pos should be(initialWheel.pos)
      }
    }
  }
}
