package machines.enigma

case class Plugboard(mapping: Map[Char, Char]) {
  def swap(c: Char): Char = mapping.getOrElse(c, c)
}

object Plugboard {
  def apply(pairs: (Char, Char)*): Plugboard = {
    val map = pairs.flatMap { case (a, b) => Seq(a -> b, b -> a) }.toMap
    new Plugboard(map)
  }
}
