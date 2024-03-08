package machines.enigma

case class Plugboard(mapping: Map[Char, Char]) {

  def passThrough(c: Char): Char = mapping.getOrElse(c, c)

  private def isValidMapping: Boolean = {
    mapping.keys.forall(_.isUpper) &&
    mapping.values.forall(_.isUpper) &&
    mapping.keys.toSet.intersect(mapping.values.toSet).isEmpty &&
    mapping.keys.size == mapping.values.toSet.size
  }
  require(isValidMapping, "Invalid plugboard mapping provided.")
}

object Plugboard {
  import scala.util.Random

  def createWithRandomMappings(): Plugboard = {
    val alphabet = ('A' to 'Z').toList
    val shuffled = Random.shuffle(alphabet)
    val pairsToMake = shuffled.length / 2 // Ensures even number for pairing
    val pairs = (shuffled.take(pairsToMake) zip shuffled.slice(
      pairsToMake,
      pairsToMake + pairsToMake
    )).flatMap { case (a, b) => List(a -> b, b -> a) }.toMap

    Plugboard(pairs)
  }
}
