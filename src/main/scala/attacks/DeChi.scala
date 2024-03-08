package attacks

import commons.Baudot.BitString
import machines.lorenz.{LorenzMachine, Wheel}

object DeChi {
  def getStartingPositions(chi1: Wheel, chi2: Wheel): Seq[(Int, Int)] = {
    for {
      i <- chi1.pins.indices
      j <- chi2.pins.indices
    } yield (i, j)
  }

  /** Runs a De-𝝌 attack on wheels/impulses 1 and 2 of delta'd Lorenz ciphertext (Z)
    *
    * @param machine: LorenzMachine - Machine with the starting positions of the chi wheels that we want to test
    * @param deltaZ: Seq[BitString] - The sequence of delta'd ciphertext we want to attack
    * @return
    */
  def runDeChi2(
      machine: LorenzMachine,
      deltaZ: Seq[BitString]
  ): Double = {
    val xorResults = deltaZ
      .foldLeft((Seq.empty[Int], machine)) {
        case ((results, currentMachine), deltaZImpulse: BitString) =>
          val chi1 = currentMachine.chi.head
          val chi2 = currentMachine.chi(1)
          val xorResult = (deltaZImpulse.charAt(0) - '0') ^ (deltaZImpulse
            .charAt(1) - '0') ^ chi1.getDelta ^ chi2.getDelta
          (results :+ xorResult, currentMachine.updateState())
      }
      ._1

    // Calculate the percentage of 0s
    val zeroCount = xorResults.count(_ == 0)
    (zeroCount.toDouble / xorResults.size) * 100
  }
}
