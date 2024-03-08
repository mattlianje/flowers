package machines.enigma

import commons.CipherMachine

/** Represents an Enigma machine with its core components: rotors, a reflector, and a plugboard.
  *
  * @param rotors A list of rotors in the order (R -> L) they are arranged in the machine.
  * @param reflector The reflector used to reverse the path of the electrical signal.
  * @param plugboard The plugboard used for an additional layer of substitution after rotor processing.
  */
case class EnigmaMachine(
    rotors: List[Rotor],
    reflector: Reflector,
    plugboard: Plugboard
) extends CipherMachine {

  /** Processes a character through the rotors in the specified direction.
    *
    * @param input     The character to be processed
    * @param direction The direction (Forward/Reverse) in which to process the character through the rotors.
    * @return An Option[Char] representing the processed character ... None <=> input None
    */
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

  /** Encrypts a single character using the Enigma machine's current configuration.
    *
    * @param input The character to be encrypted.
    * @return An Option[Char] representing the encrypted character
    *         None if the char is invalid (keep in mind Enigmas technically
    *         reasoned in terms of A-Z strictly upper case)
    */
  private def encryptChar(input: Char): Option[Char] = {
    for {
      _ <- Option(input).filter(_.isLetter).filter(_.isUpper)
      afterRotorsForward <- processThroughRotors(Some(input), Forward)
      afterReflector <- reflector.reflect(afterRotorsForward)
      afterRotorsReverse <- processThroughRotors(Some(afterReflector), Reverse)
    } yield plugboard.passThrough(afterRotorsReverse)
  }

  /** Advances the rotors by one position,
    *  simulating the actuation of the machine's mechanism after a key press.
    *
    * @return A new EnigmaMachine instance with advanced rotors.
    */
  private def advanceRotors: EnigmaMachine = {
    val (advancedRotors, _) = rotors.foldRight((List.empty[Rotor], false)) {
      case (rotor, (acc, shouldAdvanceNext)) =>
        val (turnedRotor, advanceNext) =
          if (shouldAdvanceNext || rotor.position == rotor.notch) rotor.turn
          else (rotor, false)
        (turnedRotor :: acc, advanceNext)
    }
    this.copy(rotors = advancedRotors)
  }

  /** Encrypts/decrypts a message string using the Enigma machine.
    *
    * @param message The message to be encrypted.
    * @return Either a String representing the encrypted message or an error message
    *         if an invalid character is encountered.
    */
  def encrypt(message: String): Either[RuntimeException, String] = {
    message.toUpperCase
      .foldLeft((this, Right(""): Either[RuntimeException, String])) {
        case ((machine, Right(acc)), char) =>
          machine.encryptChar(char) match {
            case Some(encryptedChar) =>
              (machine.advanceRotors, Right(acc + encryptedChar))
            case None =>
              (
                machine,
                Left(
                  new RuntimeException(s"Invalid character in message: $char")
                )
              )
          }
        case ((machine, Left(error)), _) => (machine, Left(error))
      }
      ._2
  }
}

object EnigmaMachine {
  def getDefault(): EnigmaMachine = {
    val rotor1 = Rotor.typeII('A', 'B')
    val rotor2 = Rotor.typeIII('A', 'B')
    val rotor3 = Rotor.typeIV('A', 'B')
    val reflector = Reflector.ukwB
    val plugboard = Plugboard(Map.empty)
    EnigmaMachine(List(rotor1, rotor2, rotor3), reflector, plugboard)
  }
}
