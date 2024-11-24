package machines.enigma

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
