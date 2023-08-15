package lorenz

import commons.Baudot.{bitwiseXOR, getBits, getCharacter}

object Lorenz {

  // χ1, χ2, χ3, χ4, χ5
  private val CHI_PINS: List[Int] = List(41, 31, 29, 26, 23)
  // ψ1, ψ2, ψ3, ψ4, ψ5
  private val PSI_PINS: List[Int] = List(43, 47, 51, 53, 59)
  // μ1, μ2
  private val MU_PINS: List[Int] = List(37, 61)

  case class Wheel(pins: List[Int], pos: Int) {
    def rotate: Wheel = copy(pos = (pos + 1) % pins.size)
    def bit: Int = pins(pos)
  }

  object Wheel {
    def loadDefaultWheel(pins: Int): Wheel =
      Wheel((0 until pins).toList.map(_ % 2), 0)
  }

  /** Lorenz Machine action upon each key press
    *
    * 1. μ1 ∧ χ wheels turn
    * 2. μ2 turns ↔ μ1 == 1
    * 3. μ1 ⊕ μ2 == 1 → all ψ wheels are turned
    * ~(μ1 ⊕ μ2 == 1) → ψ wheels don't turn
    */
  case class LorenzState(
      mu1: Wheel,
      mu2: Wheel,
      chi: List[Wheel],
      psi: List[Wheel]
  ) {
    def rotateMu1: LorenzState = copy(mu1 = mu1.rotate)

    def rotateMu2: LorenzState = mu1.bit match {
      case 1 => copy(mu2 = mu2.rotate)
      case _ => this
    }

    def rotateChi: LorenzState = copy(chi = chi.map(_.rotate))

    def rotatePsi: LorenzState = (mu1.bit, mu2.bit) match {
      case (0, 1) | (1, 0) => copy(psi = psi.map(_.rotate))
      case _               => this
    }

    def updateState: LorenzState = rotateChi.rotateMu1.rotateMu2.rotatePsi
  }

  case class LorenzMachine(state: LorenzState) {
    def encipher(
        input: String
    ): Either[RuntimeException, (String, LorenzMachine)] = {
      input.foldLeft(
        Right(("", this)): Either[RuntimeException, (String, LorenzMachine)]
      ) {
        case (Right((prev, machine)), char) =>
          for {
            inputBits <- getBits(char.toString).toRight(
              new RuntimeException(
                s"No bit representation found for character: $char"
              )
            )
            encipheredChar <- machine.encipherChar(inputBits)
          } yield (
            prev + encipheredChar,
            LorenzMachine(machine.state.updateState)
          )

        case (left, _) => left
      }
    }

    private def encipherChar(
        inputBits: String
    ): Either[RuntimeException, String] = {
      val chi = state.chi.map(_.bit).mkString
      val psi = state.psi.map(_.bit).mkString
      val xorBits = bitwiseXOR(chi, psi)
      val encipheredBits = bitwiseXOR(inputBits, xorBits)

      getCharacter(encipheredBits) match {
        case Some(char) => Right(char)
        case None =>
          Left(
            new RuntimeException(
              s"Failed to get character for bits: $encipheredBits"
            )
          )
      }
    }
  }

  object LorenzMachine {
    def createMachine(
        chi: Option[List[Wheel]] = None,
        psi: Option[List[Wheel]] = None,
        mu: Option[List[Wheel]] = None
    ): Either[String, LorenzMachine] = {

      def createWheel(
          userWheelsOpt: Option[List[Wheel]],
          defaultPins: List[Int]
      ): Either[String, List[Wheel]] =
        userWheelsOpt match {
          case Some(userWheels)
              if validConfiguration(userWheels, defaultPins) =>
            Right(userWheels)
          case None => Right(defaultPins.map(Wheel.loadDefaultWheel))
          case _    => Left("Incorrect wheel configuration provided.")
        }

      def validConfiguration(
          userWheels: List[Wheel],
          pins: List[Int]
      ): Boolean =
        userWheels.length == pins.length && userWheels.zip(pins).forall {
          case (wheel, pinCount) => wheel.pins.length == pinCount
        }

      for {
        chi <- createWheel(chi, CHI_PINS)
        psi <- createWheel(psi, PSI_PINS)
        mu <- createWheel(mu, MU_PINS)
      } yield LorenzMachine(LorenzState(mu.head, mu.last, chi, psi))
    }
  }
}
