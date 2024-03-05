package enigma

case class Rotor(
    letterRoll: String,
    position: Char,
    notch: Char,
    ring: Char,
    model: String
) {
  val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  def positionOf(c: Char): Option[Int] = alphabet.indexOf(c) match {
    case -1    => None
    case index => Some(index)
  }

  def offsetPosition(pos: Int): Int = {
    (pos + positionOf(position).getOrElse(0) + positionOf(ring).getOrElse(
      0
    )) % alphabet.length
  }

  def turn: (Rotor, Boolean) = {
    val nextPosition = letterRoll(
      (letterRoll.indexOf(position) + 1) % letterRoll.length
    )
    (copy(position = nextPosition), nextPosition == notch)
  }

  def passThroughForward(input: Option[Char]): Option[Char] = input.flatMap {
    c =>
      positionOf(c).flatMap { inputPos =>
        val offsetInputPos = offsetPosition(inputPos)
        Some(letterRoll(offsetInputPos))
      }
  }

  def passThroughReverse(input: Option[Char]): Option[Char] = input.flatMap {
    c =>
      letterRoll.indexOf(c) match {
        case -1 => None
        case letterPos =>
          val offsetLetterPos = (letterPos + alphabet.length - positionOf(
            position
          ).getOrElse(0) - positionOf(ring).getOrElse(0)) % alphabet.length
          Some(alphabet(offsetLetterPos))
      }
  }

}

object Rotor {
  def typeI(position: Char, ring: Char): Rotor =
    Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", position, 'R', ring, "type I")

  def typeII(position: Char, ring: Char): Rotor =
    Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", position, 'F', ring, "type II")

  def typeIII(position: Char, ring: Char): Rotor =
    Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", position, 'W', ring, "type III")

  def typeIV(position: Char, ring: Char): Rotor =
    Rotor("ESOVPZJAYQUIRHXLNFTGKDCMWB", position, 'K', ring, "type IV")

  def typeV(position: Char, ring: Char): Rotor =
    Rotor("VZBRGITYUPSDNHLXAWMJQOFECK", position, 'A', ring, "type V")
}
