package lorenz

import scala.util.Random
import LorenzMachine.{CHI_PINS, PSI_PINS, MU_PINS}

object LorenzGenerator {
  private def generateRandomPins(pinCount: Int): List[Int] = {
    val half = pinCount / 2
    val initialPins = List.fill(half)(0) ++ List.fill(pinCount - half)(1)
    Random.shuffle(initialPins)
  }

  private def generateRandomWheels(pinsList: List[Int]): List[Wheel] = {
    pinsList.map(pinCount =>
      Wheel(generateRandomPins(pinCount), Random.nextInt(pinCount))
    )
  }

  def generateRandomLorenzMachine(): LorenzMachine = {
    val chiWheels = generateRandomWheels(CHI_PINS)
    val psiWheels = generateRandomWheels(PSI_PINS)
    val muWheels = generateRandomWheels(MU_PINS)

    LorenzMachine(muWheels.head, muWheels(1), chiWheels, psiWheels)
  }

  def withRandomChiPositions(machine: LorenzMachine): LorenzMachine = {
    val randomChiWheels =
      machine.chi.map(w => w.copy(pos = Random.nextInt(w.pins.size)))
    machine.copy(chi = randomChiWheels)
  }
}
