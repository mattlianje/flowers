package flowers

import scala.io.Source
import scala.util.{Try, Success, Failure}

sealed trait BaudotToken
case class LetterToken(char: Char) extends BaudotToken
case class FigureToken(char: Char) extends BaudotToken
case object CarriageReturnToken extends BaudotToken
case object LineFeedToken extends BaudotToken
case object SpaceToken extends BaudotToken
case object LetterShiftToken extends BaudotToken
case object FigureShiftToken extends BaudotToken

sealed trait ShiftMode
case object LetterShift extends ShiftMode
case object FigureShift extends ShiftMode

/** String representation of a Baudot unit (5 1's or 0's)
 */
sealed abstract case class Baudot private (val bits: String) {
  def xor(that: Baudot): Baudot = {
    val xorBits = this.bits
      .zip(that.bits)
      .map { case (a, b) =>
        ((a - '0') ^ (b - '0')).toString
      }
      .mkString
    new Baudot(xorBits) {}
  }

  def getBaudotToken(shiftMode: ShiftMode): BaudotToken = {
    Baudot.BAUDOT_MAP.get(this) match {
      case Some((letterToken, figureToken)) =>
        shiftMode match {
          case LetterShift => letterToken
          case FigureShift => figureToken
        }
      case None =>
        throw new IllegalArgumentException(s"Invalid Baudot code: $bits")
    }
  }
}

object Baudot {

  def apply(bits: String): Option[Baudot] = fromString(bits)

  /** 1935 Continental Baudot: https://en.wikipedia.org/wiki/Baudot_code
   */
  private val BAUDOT_MAP: Map[Baudot, (BaudotToken, BaudotToken)] = Map(
    Baudot("00000").get -> (SpaceToken, SpaceToken),
    Baudot("00001").get -> (LetterToken('A'), FigureToken('1')),
    Baudot("00010").get -> (LetterToken('E'), FigureToken('2')),
    Baudot("00011").get -> (LineFeedToken, LineFeedToken),
    Baudot("00100").get -> (LetterToken('Y'), FigureToken('3')),
    Baudot("00101").get -> (LetterToken('U'), FigureToken('4')),
    Baudot("00110").get -> (LetterToken('I'), FigureToken('5')),
    Baudot("00111").get -> (LetterToken('O'), FigureToken('9')),
    Baudot("01000").get -> (LetterToken('J'), FigureToken('0')),
    Baudot("01001").get -> (LetterToken('G'), FigureToken('8')),
    Baudot("01010").get -> (LetterToken('H'), FigureToken('7')),
    Baudot("01011").get -> (LetterToken('B'), FigureToken('-')),
    Baudot("01100").get -> (LetterToken('C'), FigureToken(':')),
    Baudot("01101").get -> (LetterToken('F'), FigureToken('(')),
    Baudot("01110").get -> (LetterToken('D'), FigureToken(')')),
    Baudot("01111").get -> (LetterToken('L'), FigureToken('+')),
    Baudot("10000").get -> (LetterToken('Z'), FigureToken('%')),
    Baudot("10001").get -> (LetterToken('S'), FigureToken('?')),
    Baudot("10010").get -> (LetterToken('T'), FigureToken('=')),
    Baudot("10011").get -> (LetterToken('R'), FigureToken('/')),
    Baudot("10100").get -> (LetterToken('X'), FigureToken('&')),
    Baudot("10101").get -> (LetterToken('V'), FigureToken('$')),
    Baudot("10110").get -> (LetterToken('W'), FigureToken('!')),
    Baudot("10111").get -> (LetterToken('P'), FigureToken(';')),
    Baudot("11000").get -> (LetterToken('Q'), FigureToken('.')),
    Baudot("11001").get -> (LetterToken('M'), FigureToken(',')),
    Baudot("11010").get -> (LetterToken('N'), FigureToken('\'')),
    Baudot("11011").get -> (CarriageReturnToken, CarriageReturnToken),
    Baudot("11100").get -> (LetterToken('K'), FigureToken('6')),
    Baudot("11101").get -> (FigureShiftToken, FigureShiftToken),
    Baudot("11110").get -> (LetterShiftToken, LetterShiftToken),
    Baudot("11111").get -> (SpaceToken, SpaceToken)
  )

  val LETTERS: Set[Char] = BAUDOT_MAP.values.collect {
    case (LetterToken(char), _) => char
  }.toSet
  val FIGURES: Set[Char] = BAUDOT_MAP.values.collect {
    case (_, FigureToken(char)) => char
  }.toSet

  def tokenToBaudot(token: BaudotToken): Option[Baudot] = {
    BAUDOT_MAP.collectFirst {
      case (baudot, (letterToken, figureToken))
        if letterToken == token || figureToken == token =>
        baudot
    }
  }

  /** Smart constructor to ensure only valid baudots are ever instantiated <=>
   * in BAUDOT_MAP <=> contains 1 or 0
   */
  def fromString(bitString: String): Option[Baudot] = {
    if (bitString.length == 5 && bitString.forall(c => c == '0' || c == '1')) {
      Some(new Baudot(bitString) {})
    } else {
      None
    }
  }

  /** A Baudot + a ShiftMode (Letter or Figure) maps to one and only one
   * BaudotToken
   */
  def getToken(baudot: Baudot, mode: ShiftMode): Option[BaudotToken] = {
    BAUDOT_MAP.get(baudot).map { case (letter, figure) =>
      mode match {
        case LetterShift => letter
        case FigureShift => figure
      }
    }
  }

  /** Tokenizes string into BaudotTokens assuming a start mode of LetterShift as
   * was most common with époque machines
   */
  def tokenize(
                input: String,
                startMode: ShiftMode = LetterShift
              ): Try[List[BaudotToken]] = {
    val pattern =
      """(\[FigureShift\]|\[LetterShift\]|\[LineFeed\]|\[CarriageReturn\]|\s|\n|\r|[^\[\]])""".r
    Try {
      pattern
        .findAllIn(input)
        .toList
        .foldLeft((List.empty[BaudotToken], startMode: ShiftMode)) {
          case ((acc, currentMode), token) =>
            token match {
              case "[FigureShift]"     => (acc :+ FigureShiftToken, FigureShift)
              case "[LetterShift]"     => (acc :+ LetterShiftToken, LetterShift)
              case "[LineFeed]" | "\n" => (acc :+ LineFeedToken, currentMode)
              case "[CarriageReturn]" | "\r" =>
                (acc :+ CarriageReturnToken, currentMode)
              case " " => (acc :+ SpaceToken, currentMode)
              case c if LETTERS.contains(c.head) =>
                if (currentMode == FigureShift)
                  (acc :+ LetterShiftToken :+ LetterToken(c.head), LetterShift)
                else (acc :+ LetterToken(c.head), LetterShift)
              case c if FIGURES.contains(c.head) =>
                if (currentMode == LetterShift)
                  (acc :+ FigureShiftToken :+ FigureToken(c.head), FigureShift)
                else (acc :+ FigureToken(c.head), FigureShift)
              case _ =>
                throw new IllegalArgumentException(
                  s"Invalid Baudot character: $token"
                )
            }
        }
        ._1
    }
  }

  def tokensToString(tokens: List[BaudotToken]): String = {
    tokens
      .foldLeft((new StringBuilder, LetterShift: ShiftMode)) {
        case ((sb, currentMode), token) =>
          token match {
            case LetterShiftToken =>
              sb.append("[LetterShift]")
              (sb, LetterShift)

            case FigureShiftToken =>
              sb.append("[FigureShift]")
              (sb, FigureShift)

            case LineFeedToken =>
              sb.append("\n")
              (sb, currentMode)

            case CarriageReturnToken =>
              sb.append("\r")
              (sb, currentMode)

            case SpaceToken =>
              sb.append(" ")
              (sb, currentMode)

            case LetterToken(char) =>
              sb.append(char)
              (sb, LetterShift)

            case FigureToken(char) =>
              sb.append(char)
              (sb, FigureShift)
          }
      }
      ._1
      .toString()
  }
}

trait FileService {

  final def ENGLISH_FILE: List[String] = {
    Source.fromResource("zitadelle-en.txt").getLines().toList
  }

  final def GERMAN_FILE: List[String] = {
    Source.fromResource("zitadelle-de.txt").getLines().toList
  }
}

trait Machine {
  def encrypt(input: String): Try[String]
  def encryptStream(input: LazyList[String]): LazyList[Try[String]]
}
