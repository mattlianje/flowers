package lorenz

object Baudot {
  val baudotMap: Map[String, List[String]] = Map(
    "00000" -> List("Blank", "Blank"),
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
    "11111" -> List("Letter Shift", "")
  )

  // Add mapping from string to bits
  private val inverseBaudotMap: Map[String, String] =
    baudotMap.flatMap { case (k, v) => v.map(_ -> k) }

  def getCharacter(bits: String, shift: Boolean = false): Option[String] = {
    baudotMap.get(bits).flatMap(l => if (shift) l.lift(1) else l.headOption)
  }

  def getBits(character: String): Option[String] = {
    inverseBaudotMap.get(character)
  }

  def bitwiseXOR(s1: String, s2: String): String = {
    val int1 = Integer.parseInt(s1, 2)
    val int2 = Integer.parseInt(s2, 2)
    val xorResult = int1 ^ int2
    val xorBinaryString = Integer.toBinaryString(xorResult)
    // Ensuring the output always has 5 bits by adding leading zeros if necessary
    String.format("%5s", xorBinaryString).replace(' ', '0')
  }

  def main(args: Array[String]): Unit = {
    println(inverseBaudotMap)
    println(getCharacter("10011"))
    println(getBits("B"))
  }
}
