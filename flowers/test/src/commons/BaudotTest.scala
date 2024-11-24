package commons


import commons.Baudot._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.util.{Try, Success, Failure}

class BaudotSpecs extends AnyFlatSpec with Matchers {

  "Baudot.tokenize" should "correctly tokenize a string with shifts and special characters" in {
    val input = "AB1[FigureShift]CD"
    val tokens = Baudot.tokenize(input)
    tokens shouldBe Success(List(
      LetterToken('A'),
      LetterToken('B'),
      FigureShiftToken,
      FigureToken('1'),
      FigureShiftToken,
      LetterShiftToken,
      LetterToken('C'),
      LetterToken('D')
    ))
  }

  it should "handle strings with only letters" in {
    val input = "ABC"
    val tokens = Baudot.tokenize(input)
    tokens shouldBe Success(List(
      LetterToken('A'),
      LetterToken('B'),
      LetterToken('C')
    ))
  }

  it should "handle strings with only figures" in {
    val input = "123"
    val tokens = Baudot.tokenize(input)
    tokens shouldBe Success(List(
      FigureShiftToken,
      FigureToken('1'),
      FigureToken('2'),
      FigureToken('3')
    ))
  }

  it should "handle mixed strings without explicit shifts" in {
    val input = "A1B2"
    val tokens = Baudot.tokenize(input)
    tokens shouldBe Success(List(
      LetterToken('A'),
      FigureShiftToken,
      FigureToken('1'),
      LetterShiftToken,
      LetterToken('B'),
      FigureShiftToken,
      FigureToken('2')
    ))
  }

  it should "return tokens for valid characters and skip invalid characters" in {
    val input = "ABC@"
    val tokens = Baudot.tokenize(input)
    tokens shouldBe a [Failure[_]]
    tokens.failed.get shouldBe a [IllegalArgumentException]
  }
}