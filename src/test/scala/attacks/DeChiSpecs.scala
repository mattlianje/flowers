package attacks

import attacks.DeChi.runDeChi2
import commons.Baudot.{BitString, getDelta}
import commons.Language.German
import commons.PlaintextReader.loadLorenzBitStream
import lorenz.{LorenzGenerator, LorenzMachine, Wheel}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class DeChiSpecs extends AnyFlatSpec with Matchers {

  private val TEST_MACHINE: LorenzMachine =
    LorenzGenerator.generateRandomLorenzMachine()

  private val BITSTRINGS_ZITADELLE: Seq[BitString] = loadLorenzBitStream(
    German
  ) match {
    case Some(value) => value
    case None        => Seq.empty[BitString]
  }

  "DeChi" should "generate correct starting positions" in {
    val chi1 = Wheel(List(0, 1), 0)
    val chi2 = Wheel(List(1, 0), 0)
    val expectedPositions = List((0, 0), (0, 1), (1, 0), (1, 1))

    DeChi.getStartingPositions(chi1, chi2) shouldBe expectedPositions
  }

  it should "compute zero ratio correctly" in {
    val inputBitStrings: Seq[BitString] = Seq("10001", "00000")
    val encipheredResult = TEST_MACHINE.encipher(inputBitStrings)

    encipheredResult match {
      case Left(e) =>
        fail(s"Enciphering failed with exception: ${e.getMessage}")
      case Right(encipheredBitStrings) =>
        val result = DeChi.runDeChi2(TEST_MACHINE, encipheredBitStrings)
        result should be > 0.0
        result should be <= 100.0
    }
  }

  it should "have a higher success percentage with the correct starting positions" in {
    TEST_MACHINE.encipher(BITSTRINGS_ZITADELLE) match {
      case Right(enciphered) =>
        getDelta(enciphered.toList) match {
          case Some(deltaZ) =>
            val WRONG_MACHINE =
              LorenzGenerator.withRandomChiPositions(TEST_MACHINE)
            val wrongResult = runDeChi2(WRONG_MACHINE, deltaZ)
            val correctResult = runDeChi2(TEST_MACHINE, deltaZ)

            correctResult should be > wrongResult
          case None =>
            fail("Unable to compute delta for the enciphered string.")
        }
      case Left(exception) =>
        fail(s"Enciphering failed with exception: $exception")
    }
  }

}
