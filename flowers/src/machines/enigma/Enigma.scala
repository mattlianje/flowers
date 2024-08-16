package machines.enigma

import commons._
import cats.data.State
import cats.implicits._
import scala.util.matching.Regex
import scala.util.{Try, Success, Failure}


case class EnigmaMachine(
    plugboard: Plugboard,
    rotors: List[Rotor],
    reflector: Reflector
) extends Machine {

  def encodeChar(c: Char): State[EnigmaMachine, Char] = State { machine =>
    val finalOutput = (
      machine.plugboard.swap _
        andThen Rotor.composeRotors(machine.rotors)
        andThen machine.reflector.reflect
        andThen Rotor.composeRotors(machine.rotors, reverse = true)
        andThen machine.plugboard.swap
    )(c)
    val rotatedRotors = Rotor.rotateRotors(machine.rotors)
    (machine.copy(rotors = rotatedRotors), finalOutput)
  }

  def encodeString(input: String): State[EnigmaMachine, String] =
    input.toList.traverse(encodeChar).map(_.mkString)

  def encrypt(input: String): Try[String] = {
    EnigmaMachine.validateInput(input) match {
      case Some(validInput) =>
        val initialMachine = this
        Success(encodeString(validInput).runA(initialMachine).value)
      case None =>
        Failure(new IllegalArgumentException(s"Invalid input string: $input"))
    }
  }
}

object EnigmaMachine {
  val validChars: Set[Char] = ('A' to 'Z').toSet

  def validateInput(input: String): Option[String] = {
    val validPattern = "^[A-Z]+$".r
    validPattern.findFirstIn(input)
  }
}
