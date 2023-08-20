package commons

import commons.ShiftMode._

import scala.annotation.tailrec

object Baudot {

  type BitString = String
  type Character = String

  private val L_SHIFT: BitString = "11111"
  private val F_SHIFT: BitString = "11011"

  private val baudotMap: Map[String, List[String]] = Map(
    "00000" -> List("_", "_"), // Blank
    "00001" -> List("T", "5"),
    "00010" -> List("\n", "\n"), // Carriage Return
    "00011" -> List("O", "9"),
    "00100" -> List(" ", " "), // Space
    "00101" -> List("H", ""),
    "00110" -> List("N", ","),
    "00111" -> List("M", "."),
    "01000" -> List("\f", "\f"), // Line Feed
    "01001" -> List("L", ")"),
    "01010" -> List("R", "4"),
    "01011" -> List("G", "&"),
    "01100" -> List("I", "8"),
    "01101" -> List("P", "0"),
    "01110" -> List("C", ":"),
    "01111" -> List("V", ";"),
    "10000" -> List("E", "3"),
    "10001" -> List("Z", "\""),
    "10010" -> List("D", "$"),
    "10011" -> List("B", "?"),
    "10100" -> List("S", "*"), // BEL
    "10101" -> List("Y", "6"),
    "10110" -> List("F", "!"),
    "10111" -> List("X", "/"),
    "11000" -> List("A", "-"),
    "11001" -> List("W", "2"),
    "11010" -> List("J", "'"),
    "11011" -> List("FigShift", ""),
    "11100" -> List("U", "7"),
    "11101" -> List("Q", "1"),
    "11110" -> List("K", "("),
    "11111" -> List("LtShift", "")
  )

  def bitwiseXOR(s1: BitString, s2: BitString): Option[BitString] = {
    (s1, s2) match {
      case (a, b)
          if a.length == b.length && a.forall(c => c == '0' || c == '1') && b
            .forall(c => c == '0' || c == '1') =>
        Some(
          a.lazyZip(b)
            .map { case (c1, c2) => if (c1 == c2) '0' else '1' }
            .mkString
        )
      case _ => None
    }
  }

  def getCharacter(
      bits: BitString,
      mode: ShiftMode.Value
  ): Option[Character] = {
    baudotMap.get(bits).flatMap {
      case List(letter: Character, figure: Character) =>
        mode match {
          case L => Some(letter)
          case F => Some(figure)
          case _ => None
        }
      case _ => None
    }
  }

  def getBits(character: Character): Option[BitString] = {
    baudotMap
      .find {
        case (_, List(letter, figure)) =>
          character == letter || character == figure
        case _ => false
      }
      .map(_._1)
  }

  def stringToBaudotChunks(input: String): Option[List[BitString]] = {

    def determineMode(
        char: Char,
        currentMode: ShiftMode.Value
    ): (ShiftMode.Value, List[BitString]) = char match {
      case letter if letter.isLetter && currentMode != L =>
        (L, List(L_SHIFT))
      case figure
          if (!figure.isLetter && "!&'():;?$\"0123456789".contains(
            figure
          )) && currentMode != F =>
        (F, List(F_SHIFT))
      case _ => (currentMode, List.empty)
    }

    @tailrec
    def processString(
        chars: List[Char],
        currentMode: ShiftMode.Value,
        acc: List[BitString]
    ): Option[List[BitString]] = chars match {
      case Nil => Some(acc)
      case char :: tail =>
        val (newMode, modeBits) = determineMode(char, currentMode)
        getBits(char.toString) match {
          case Some(charBit) =>
            processString(tail, newMode, acc ++ modeBits :+ charBit)
          case None => None
        }
    }
    processString(input.toUpperCase().toList, L, Nil)
  }

  def baudotChunksToString(chunks: List[BitString]): Option[String] = {
    val initial: (ShiftMode.Value, String) = (L, "")

    val (_, resultString) = chunks.foldLeft(initial) {
      case ((_, acc), F_SHIFT) => (F, acc)
      case ((_, acc), L_SHIFT) => (L, acc)
      case ((currentMode, acc), bit) =>
        getCharacter(bit, currentMode) match {
          case Some(character) => (currentMode, acc + character)
          case None            => return None
        }
    }

    Some(resultString)
  }

  def getDelta(bitStrings: List[BitString]): Option[List[BitString]] = {
    @tailrec
    def computeDelta(
        strings: List[BitString],
        acc: List[BitString]
    ): Option[List[BitString]] = strings match {
      case first :: second :: tail =>
        bitwiseXOR(first, second) match {
          case Some(result) => computeDelta(second :: tail, acc :+ result)
          case None         => None
        }
      case _ => Some(acc)
    }

    computeDelta(bitStrings, List.empty)
  }

}
