package machines.lorenz

case class Wheel(pins: List[Int], pos: Int) {
  require(
    pos >= 0 && pos < pins.size,
    "Position should be between 0 and (number of pins - 1)"
  )
  def rotate: Wheel = copy(pos = (pos + 1) % pins.size)
  def bit: Int = pins(pos)
  def getDelta: Int = {
    val nextPosBit = pins((pos + 1) % pins.size)
    bit ^ nextPosBit
  }
}

object Wheel {
  def loadDefault(pins: Int): Wheel =
    Wheel((0 until pins).toList.map(_ % 2), 0)
}
