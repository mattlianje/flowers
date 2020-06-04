package lorenz

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

  def getChar(binary: String, mode: Int): String = {
    if (mode == 0) {
      val value = baudot_map.get(binary).toList.head.toString()
      value
    }
    else {
      val value = baudot_map.get(binary).toList(1).toString()
      value
    }
  }

  def XOR(x: String, y: String, mode: Int): String = {
    val xInt = x.toInt
    val yInt = y.toInt
    val performXOR = xInt ^ yInt
    println(performXOR)
    baudot_map.get(performXOR.toString).toList(mode).toString()
  }
}
