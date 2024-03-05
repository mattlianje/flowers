package commons

import commons.Baudot._
import commons.ShiftMode._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class BaudotSpecs extends AnyFlatSpec with Matchers {

  "getCharacter" should "return the correct character for given bits without shift" in {
    getCharacter("00000", L) shouldBe Some("_")
    getCharacter("00001", L) shouldBe Some("T")
    getCharacter("00100", L) shouldBe Some(" ")
    getCharacter("invalidBitPattern", L) shouldBe None
  }

  it should "return the correct character for given bits with figure shift" in {
    getCharacter("00000", F) shouldBe Some("_")
    getCharacter("00001", F) shouldBe Some("5")
    getCharacter("00100", F) shouldBe Some(" ")
    getCharacter("11111", F) shouldBe Some("")
  }

  "getBits" should "return the correct bits for a given character" in {
    getBits(" ") shouldBe Some("00100")
    getBits(",") shouldBe Some("00110")
    getBits("T") shouldBe Some("00001")
    getBits("unknownChar") shouldBe None
  }

  "bitwiseXOR" should "return the correct result for given bit strings" in {
    bitwiseXOR("00001", "00100") shouldBe Some("00101")
    bitwiseXOR("00000", "11111") shouldBe Some("11111")
    bitwiseXOR("101010", "010101") shouldBe Some("111111")
  }

  it should "return None for invalid bit strings" in {
    bitwiseXOR("invalid", "00100") shouldBe None
    bitwiseXOR("00100", "invalid") shouldBe None
    bitwiseXOR("invalid", "invalid") shouldBe None
    bitwiseXOR("001", "00100") shouldBe None
    bitwiseXOR("00100", "001") shouldBe None
  }

  "stringToBaudotChunks" should "return a sequence of bit strings for a valid input string" in {
    stringToBaudotChunks("To") shouldBe Some(
      Seq("00001", "00011")
    ) // Testing that lowercase is handled nicely as well
    stringToBaudotChunks("9") shouldBe Some(
      Seq("11011", "00011")
    ) // Needs to add a F (FigureShift) before the "9"
    stringToBaudotChunks("A1B") shouldBe Some(
      Seq("11000", "11011", "11101", "11111", "10011")
    ) // Testing we toggle to F mode and back to L mode
    stringToBaudotChunks("Ê") shouldBe None
  }

  "Baudot's getDelta" should "compute the delta between consecutive BitStrings in a list" in {
    val bitStrings = List("10010", "01101", "11001")
    val expectedDeltas = List("11111", "10100") // Corrected expected delta

    val result = getDelta(bitStrings)
    result shouldEqual Some(expectedDeltas)
  }

}
