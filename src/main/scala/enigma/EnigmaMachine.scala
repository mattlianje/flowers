package enigma

sealed trait Direction
case object Forward extends Direction
case object Reverse extends Direction

case class EnigmaMachine(
    rotors: List[Rotor],
    reflector: Reflector,
    plugboard: Plugboard
) {

  private def processThroughRotors(
      input: Option[Char],
      direction: Direction
  ): Option[Char] = {
    direction match {
      case Forward =>
        rotors.foldLeft(input) { (accOption, rotor) =>
          accOption.flatMap(acc => rotor.passThroughForward(Some(acc)))
        }
      case Reverse =>
        rotors.foldRight(input) { (rotor, accOption) =>
          accOption.flatMap(acc => rotor.passThroughReverse(Some(acc)))
        }
    }
  }

  def encryptChar(input: Char): Option[Char] = {
    if (input.isLetter && input.isUpper) {
      val afterPlugboard = plugboard.passThrough(input)
      val afterRotorsForward =
        processThroughRotors(Some(afterPlugboard), Forward).getOrElse(
          afterPlugboard
        )
      val afterReflector =
        reflector.reflect(afterRotorsForward).getOrElse(return None)
      val afterRotorsReverse =
        processThroughRotors(Some(afterReflector), Reverse).getOrElse(
          afterReflector
        )
      val finalChar = plugboard.passThrough(afterRotorsReverse)
      Some(finalChar)
    } else {
      None
    }
  }

  def advanceRotors: EnigmaMachine = {
    val (advancedRotors, _) = rotors.foldRight((List.empty[Rotor], false)) {
      case (rotor, (acc, shouldAdvanceNext)) =>
        val (turnedRotor, advanceNext) =
          if (shouldAdvanceNext || rotor.position == rotor.notch) rotor.turn
          else (rotor, false)
        (turnedRotor :: acc, advanceNext)
    }
    this.copy(rotors = advancedRotors)
  }

  def encryptMessage(message: String): Either[String, String] = {
    message.toUpperCase
      .foldLeft((this, Right(""): Either[String, String])) {
        case ((machine, Right(acc)), char) =>
          machine.encryptChar(char) match {
            case Some(encryptedChar) =>
              (machine.advanceRotors, Right(acc + encryptedChar))
            case None => (machine, Left(s"Invalid character in message: $char"))
          }
        case ((machine, Left(error)), _) => (machine, Left(error))
      }
      ._2
  }
}
