package machines.enigma

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
