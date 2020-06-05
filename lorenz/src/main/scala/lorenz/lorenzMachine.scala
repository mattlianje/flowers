package lorenz

case class lorenzMachine(chiWheels: List[Wheel],
                         muI: Wheel,
                         muII: Wheel,
                         psiWheels: List[Wheel]
                         ) {

  def getChiVal: String = {
    val res = ""
    this.chiWheels.foreach(wheel => res.concat(wheel.getValue.toString))
    res
  }

  def getPsiVal: String = {
    val res = ""
    this.psiWheels.foreach(wheel => res.concat(wheel.getValue.toString))
    res
  }

  /*
   * plaintext ⊕ χstream ⊕ ψ' = ciphertext
   * note: ψ' is prime since the psi wheel movements are irregular and controlled by the μ wheels.
   */
  def encodeChar(c: String): String = {
    val chiEncoding = Baudot.XOR(Baudot.charToBaudot(c), this.getChiVal,0)
    Baudot.XOR(chiEncoding, this.getPsiVal,0)
  }
   /*
    * At each key press ...
    *   1. μ1 and χ wheels turn.
    *   2. μ2 turns iff μ1 has a value of 1.
    *   3. If μ1 ⊕ μ2 is 1 then all ψ wheels are turned, if not they don't.
    */
  def updateMachine: lorenzMachine = {
    val new_muI = this.muI.turnWheel
    val newChiWheels = this.chiWheels.map(wheel => wheel.turnWheel)
    if (new_muI.getValue == 1) {
      val new_muII = this.muII.turnWheel
      if ((new_muI.getValue ^ new_muII.getValue) == 1) {
        val newPsiWheels = this.psiWheels.map(wheel => wheel.turnWheel)
        lorenzMachine(newChiWheels, new_muI, new_muII, newPsiWheels)
      }
      else {
        lorenzMachine(newChiWheels, new_muI, new_muII, this.psiWheels)
      }
    }
    else {
      lorenzMachine(newChiWheels, new_muI, this.muII, this.psiWheels)
    }
  }

}

object Machines {
  def getTestMachine() = lorenzMachine (
    chiWheels = List(Wheels.chi_I(0),
                      Wheels.chi_II(0),
                      Wheels.chi_III(0),
                      Wheels.chi_IV(0),
                      Wheels.chi_V(0)),
    muI = Wheels.mu_I(0),
    muII = Wheels.mu_II(0),
    psiWheels = List(Wheels.psi_I(0),
                      Wheels.psi_II(0),
                      Wheels.psi_III(0),
                      Wheels.psi_IV(0),
                      Wheels.psi_V(0))
  )
}