package enigma

case class Reflector(mapping: String) {
  require(mapping.length == 26, "Reflector mapping must be 26 characters long.")
  private val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

  def reflect(c: Char): Option[Char] = {
    alphabet.indexOf(c.toUpper) match {
      case -1    => None
      case index => Some(mapping(index))
    }
  }
}

object Reflector {
  def ukwB: Reflector = Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
  def ukwC: Reflector = Reflector("FVPJIAOYEDRZXWGCTKUQSBNMHL")
}
