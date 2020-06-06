package lorenz

case class lorenzMachine(chiWheels: List[Wheel],
                         muI: Wheel,
                         muII: Wheel,
                         psiWheels: List[Wheel]
                         ) {

  def getChiVal: String = {
    val wheelVals = this.chiWheels.map(wheel => wheel.getValue.toString)
    wheelVals.mkString("")
  }

  def getPsiVal: String = {
    val wheelVals = this.psiWheels.map(wheel => wheel.getValue.toString)
    wheelVals.mkString("")
  }

  /*
   * plaintext ⊕ χstream ⊕ ψ' = ciphertext
   * note: ψ' is prime since the psi wheel movements are irregular and controlled by the μ wheels.
   */
  def encodeChar(c: String): String = {
    val chiXOR = Baudot.charToBaudot(Baudot.XOR(Baudot.charToBaudot(c), this.getChiVal, 0, ""))
    val res = Baudot.XOR(chiXOR, this.getPsiVal,0, "")
    printState(c, res)
    res
  }
   /*
    * At each ke press ...
    *   1. μ1 and χ wheels turn.
    *   2. μ2 turns iff μ1 has a value of 1.
    *   3. If μ1 ⊕ μ2 is 1 then all ψ wheels are turned, if not they don't.
    */
  def updateMachine: lorenzMachine = {
    val new_muI = this.muI.turnWheel
    val newChiWheels = this.chiWheels.map(wheel => wheel.turnWheel)
    val newPsiWheels = this.psiWheels.map(wheel => wheel.turnWheel)

    if (new_muI.getValue == 1) {
      val new_muII = this.muII.turnWheel
      if ((new_muI.getValue ^ new_muII.getValue) == 1) {
        lorenzMachine(newChiWheels, new_muI, new_muII, newPsiWheels)
      }
      else {
        lorenzMachine(newChiWheels, new_muI, new_muII, this.psiWheels)
      }
    }
    else {
      if ((new_muI.getValue ^ muII.getValue) == 1) {
        lorenzMachine(newChiWheels, new_muI, muII, newPsiWheels)
      }
      else {
        lorenzMachine(newChiWheels, new_muI, this.muII, this.psiWheels)
      }
    }
  }

  def printState(inputChar: String, outputChar: String): Unit = {
    val chiXOR = Baudot.charToBaudot(Baudot.XOR(Baudot.charToBaudot(inputChar), this.getChiVal, 0, ""))
    print("Wheel:       ")
    for (wheel <- this.chiWheels) {
      print(wheel.wheel_type + "     ")
    }
    print(this.muI.wheel_type + "     " + this.muII.wheel_type + "     ")
    for (wheel <- this.psiWheels) {
      print(wheel.wheel_type + "     ")
    }
    print("\n\nPosition:   ")
    for (wheel <- this.chiWheels) {
      print(wheel.position + "/" + wheel.number_of_pins + "     ")
    }
    print(this.muI.position + "/" + this.muI.number_of_pins + "     " + this.muII.position + "/" + this.muII.number_of_pins + "     ")
    for (wheel <- this.psiWheels) {
      print(wheel.position + "/" + wheel.number_of_pins + "     ")
    }
    print("\n\nPin setting:  ")
    for (wheel <- this.chiWheels) {
      print(wheel.getValue + "        ")
    }
    print(this.muI.getValue + "        " + this.muII.getValue + "        ")
    for (wheel <- this.psiWheels) {
      print(wheel.getValue + "        ")
    }
    print("\n\n             |________________________________________|                    |________________________________________|" +
          "\n                               χ key                                                          ψ' key" +
          "\n\n Input char: " + inputChar + " (" + Baudot.charToBaudot(inputChar) + ")" + " ⊕ χ-key (" + this.getChiVal + ") = " +
           chiXOR + " ------------------------------------>  ⊕ ψ-key (" + this.getPsiVal + ")  -------------------> "
           + Baudot.charToBaudot(outputChar) + "  Output char:" + outputChar + "\n\n")
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