package lorenz
import scala.annotation.tailrec

// Using European Continental Baudot:
// https://cs.stanford.edu/people/eroberts/courses/soco/projects/2008-09/colossus/baudot.html

object Baudot {

  val baudot_map = Map("00000" -> List("Blank", "Blank"),
  "00001" -> List("T", "5"),
  "00010" -> List("CR", "CR"),
  "00011" -> List("O", "9"),
  "00100" -> List("Space", "Space"),
  "00101" -> List("H", ""),
  "00110" -> List("N", ","),
  "00111" -> List("M", "."),
  "01001" -> List("Line Feed", "Line Feed"),
  "01001" -> List("L", ")"),
  "01010" -> List("R", "4"),
  "01011" -> List("G", "&"),
  "01100" -> List("I", "8"),
  "01101" -> List("P", "0"),
  "01110" -> List("C", ":"),
  "01111" -> List("V", ";"),
  "10000" -> List("E", "3"),
  "10001" -> List("Z", "quote"),
  "10010" -> List("D", "$"),
  "10011" -> List("B", "?"),
  "10100" -> List("S", "BEL"),
  "10101" -> List("Y", "6"),
  "10110" -> List("F", "!"),
  "10111" -> List("X", "/"),
  "11000" -> List("A", "-"),
  "11001" -> List("W", "2"),
  "11010" -> List("J", "'"),
  "11011" -> List("Figure Shift", ""),
  "11100" -> List("U", "7"),
  "11101" -> List("Q", "1"),
  "11110" -> List("K", "("),
  "11111" -> List("Letter Shift", ""))

  def getChar (binary: String, mode: Int): String = {
    if (mode == 0) {
      baudot_map.get(binary).toList.head.toString()
    }
    else {
      baudot_map.get(binary).toList(1).toString()
    }
  }

  @tailrec
  def XOR (x: String, y: String, mode: Int, curr: String): String = {
    if (curr.length == x.length) {
      if (mode == 0) {
        baudot_map.get(curr).head.head
      }
      else {
        baudot_map.get(curr).head(1)
      }
    }
    else {
      if ((x.charAt(curr.length) == '1' && y.charAt(curr.length) == '0') ||
        (x.charAt(curr.length) == '0' && y.charAt(curr.length) == '1')) {
        XOR(x, y, mode, curr.concat("1"))
      }
      else{
        XOR(x, y, mode, curr.concat("0"))
      }
    }
  }

  def charToBaudot (c: String): String = {
    val default = (-1, "")
    baudot_map.find(_._2.contains(c)).getOrElse(default)._1.toString
  }
}
