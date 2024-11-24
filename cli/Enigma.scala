package flowers

import cats.data.State
import cats.implicits._
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._
import io.circe.parser.decode
import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}
import util.chaining.scalaUtilChainingOps

case class EnigmaMachine(
    plugboard: Plugboard,
    rotors: List[Rotor],
    reflector: Reflector
) extends Machine {

  def encodeChar(c: Char): State[EnigmaMachine, Char] = State { machine =>
    val z = machine.plugboard
      .swap(c)
      .pipe(Rotor.composeRotors(machine.rotors))
      .pipe(machine.reflector.reflect)
      .pipe(Rotor.composeRotors(machine.rotors, reverse = true))
      .pipe(machine.plugboard.swap)

    (machine.copy(rotors = Rotor.rotateRotors(machine.rotors)), z)
  }

  def encodeString(input: String): State[EnigmaMachine, String] =
    input.toList.traverse(encodeChar).map(_.mkString)

  def encrypt(input: String): Try[String] = {
    EnigmaMachine
      .validateInput(input)
      .fold(
        Failure(new IllegalArgumentException(s"Invalid input string: $input"))
      )(validInput => Success(encodeString(validInput).runA(this).value))
  }

  def encryptStream(input: LazyList[String]): LazyList[Try[String]] = ???
}

object EnigmaMachine {
  def validateInput(input: String): Option[String] = {
    val validPattern = "^[A-Z]+$".r
    validPattern.findFirstIn(input)
  }

  case class RotorConfig(
    `type`: String, /* TypeI, TypeII, TypeIII, TypeIV, TypeV */
    position: Char,
    ring: Char
  )
  
  case class PlugboardConfig(pairs: List[(Char, Char)])
  
  case class EnigmaConfig(
    plugboard: PlugboardConfig,
    rotors: List[RotorConfig],
    reflector: String  /* UKWB or UKWC */
  )

  implicit val charPairDecoder: Decoder[(Char, Char)] = 
    Decoder.decodeTuple2[String, String].emap { case (a, b) =>
      if (a.length == 1 && b.length == 1) Right((a.head.toUpper, b.head.toUpper))
      else Left("Plugboard pairs must be single characters")
    }

  implicit val rotorConfigDecoder: Decoder[RotorConfig] = deriveDecoder
  implicit val plugboardConfigDecoder: Decoder[PlugboardConfig] = deriveDecoder
  implicit val enigmaConfigDecoder: Decoder[EnigmaConfig] = deriveDecoder

  def fromConfig(jsonConfig: String): Try[EnigmaMachine] = {
    decode[EnigmaConfig](jsonConfig).toTry.flatMap { config =>
      Try {
        val rotors = config.rotors.map { r =>
          val baseRotor = r.`type` match {
            case "TypeI" => Rotor.TypeI
            case "TypeII" => Rotor.TypeII
            case "TypeIII" => Rotor.TypeIII
            case "TypeIV" => Rotor.TypeIV
            case "TypeV" => Rotor.TypeV
            case t => throw new IllegalArgumentException(s"Unknown rotor type: $t")
          }
          new Rotor(baseRotor.mapping, r.position, baseRotor.notch, r.ring) {}
        }

        val reflector = config.reflector match {
          case "UKWB" => Reflector.UKWB
          case "UKWC" => Reflector.UKWC
          case r => throw new IllegalArgumentException(s"Unknown reflector: $r")
        }

        val plugboard = Plugboard(config.plugboard.pairs: _*)

        EnigmaMachine(plugboard, rotors, reflector)
      }
    }
  }
}

case class Plugboard(mapping: Map[Char, Char]) {
  def swap(c: Char): Char = mapping.getOrElse(c, c)
}

object Plugboard {
  def apply(pairs: (Char, Char)*): Plugboard = {
    val map = pairs.flatMap { case (a, b) => Seq(a -> b, b -> a) }.toMap
    new Plugboard(map)
  }
}

sealed abstract class Reflector(val mapping: Map[Char, Char]) {
  def reflect(c: Char): Char = mapping.getOrElse(c, c)
}

object Reflector {
  case object UKWB
      extends Reflector(
        (for (i <- "YRUHQSLDPXNGOKMIEBFZCWVJAT".indices)
          yield ("YRUHQSLDPXNGOKMIEBFZCWVJAT" (i), ('A' + i).toChar)).toMap
      )

  case object UKWC
      extends Reflector(
        (for (i <- "FVPJIAOYEDRZXWGCTKUQSBNMHL".indices)
          yield ("FVPJIAOYEDRZXWGCTKUQSBNMHL" (i), ('A' + i).toChar)).toMap
      )
}

sealed abstract class Rotor(
    val mapping: Map[Char, Char],
    val position: Char,
    val notch: Char,
    val ring: Char
) {
  private val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  private val n: Int = alphabet.length

  def encode(c: Char, reverse: Boolean = false): Char = {
    val idx =
      (alphabet.indexOf(c) + alphabet.indexOf(position) - alphabet.indexOf(
        ring
      ) + n) % n
    val encodedChar = if (reverse) {
      mapping.find(_._2 == alphabet(idx)).map(_._1).getOrElse(c)
    } else {
      mapping.getOrElse(alphabet(idx), c)
    }
    alphabet(
      (alphabet.indexOf(encodedChar) - alphabet.indexOf(position) + alphabet
        .indexOf(ring) + n) % n
    )
  }

  def rotate: Rotor = new Rotor(
    mapping,
    alphabet((alphabet.indexOf(position) + 1) % n),
    notch,
    ring
  ) {}
}

object Rotor {
  case object TypeI
      extends Rotor(
        (for (i <- "EKMFLGDQVZNTOWYHXUSPAIBRCJ".indices)
          yield ("EKMFLGDQVZNTOWYHXUSPAIBRCJ" (i), ('A' + i).toChar)).toMap,
        'A',
        'R',
        'A'
      )

  case object TypeII
      extends Rotor(
        (for (i <- "AJDKSIRUXBLHWTMCQGZNPYFVOE".indices)
          yield ("AJDKSIRUXBLHWTMCQGZNPYFVOE" (i), ('A' + i).toChar)).toMap,
        'A',
        'F',
        'A'
      )

  case object TypeIII
      extends Rotor(
        (for (i <- "BDFHJLCPRTXVZNYEIWGAKMUSQO".indices)
          yield ("BDFHJLCPRTXVZNYEIWGAKMUSQO" (i), ('A' + i).toChar)).toMap,
        'A',
        'W',
        'A'
      )

  case object TypeIV
      extends Rotor(
        (for (i <- "ESOVPZJAYQUIRHXLNFTGKDCMWB".indices)
          yield ("ESOVPZJAYQUIRHXLNFTGKDCMWB" (i), ('A' + i).toChar)).toMap,
        'A',
        'K',
        'A'
      )

  case object TypeV
      extends Rotor(
        (for (i <- "VZBRGITYUPSDNHLXAWMJQOFECK".indices)
          yield ("VZBRGITYUPSDNHLXAWMJQOFECK" (i), ('A' + i).toChar)).toMap,
        'A',
        'A',
        'A'
      )

  /** A uni-directional pass through multiple rotors can be seen
    * as a pass through a single new "composed" rotor
    */
  def composeRotors(
      rotors: List[Rotor],
      reverse: Boolean = false
  ): Char => Char = {
    val rotorFunctions: List[Char => Char] =
      if (reverse) {
        rotors.reverse.map(rotor =>
          (char: Char) => rotor.encode(char, reverse = true)
        )
      } else {
        rotors.map(rotor => (char: Char) => rotor.encode(char))
      }

    rotorFunctions.reduce((f, g) => f andThen g)
  }

  def rotateRotors(rotors: List[Rotor]): List[Rotor] = {
    def rotateHelper(rs: List[Rotor], carry: Boolean): List[Rotor] = rs match {
      case Nil => Nil
      case head :: tail =>
        val rotatedHead = if (carry) head.rotate else head
        rotatedHead :: rotateHelper(tail, rotatedHead.position == 0)
    }
    rotateHelper(rotors, carry = true)
  }
}
