package commons

import commons.Language._
import commons.PlaintextReader._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PlaintextReaderSpecs extends AnyFunSuite with Matchers {

  test("Should load English Lorenz plaintext correctly") {
    val englishText = loadLorenzPlaintext(English)
    englishText should not be empty
    englishText should not contain "\n"
    englishText should not contain "\r"
  }

  test("Should load German Lorenz plaintext correctly") {
    val germanText = loadLorenzPlaintext(German)
    germanText should not be empty
    germanText should not contain "\n"
    germanText should not contain "\r"
  }

  test("English and German plaintexts should not be the same") {
    val englishText = loadLorenzPlaintext(English)
    val germanText = loadLorenzPlaintext(German)
    englishText should not equal germanText
  }
}