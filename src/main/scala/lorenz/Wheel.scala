package lorenz

/** Case class representing any of the wheels on the Lorenz machines
  *  They had a different number of cams, and each can could be raised or lowered
  *
  * @param pins: List[Int], ∀ n ∈ {0,1} representing raised or lowered pins
  *              starting with position 0.
  * @param pos: Int, Current position of wheel relative to 0.
  */
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
  def loadDefaultWheel(pins: Int): Wheel =
    Wheel((0 until pins).toList.map(_ % 2), 0)

}
