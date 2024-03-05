package enigma

sealed trait Direction
case object Forward extends Direction
case object Reverse extends Direction

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
) {

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

  /** Advances the rotors by one position,
   *  simulating the actuation of the machine's mechanism after a key press.
   *
   * @return A new EnigmaMachine instance with advanced rotors.
   */
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

  /** Encrypts/decrypts a message string using the Enigma machine.
   *
   * @param message The message to be encrypted.
   * @return Either a String representing the encrypted message or an error message
   *         if an invalid character is encountered.
   */
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
