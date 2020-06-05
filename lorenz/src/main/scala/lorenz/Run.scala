package lorenz
import cats.instances.all._


object Main extends App {

  val test = Baudot.XOR("10010", "00001", 0)
  val test2 = Baudot.charToBaudot("C")
  println(test2)
  val testWheel = Wheels.chi_I(0).getRandomPins(15, List())
  for(element <- testWheel) {println(element)}

}
