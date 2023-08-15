package lorenz
import commons.Baudot.{bitwiseXOR, getBits, getCharacter}
import zio._

object LorenzZIO {

  private val CHI_PINS: List[Int] =
    List(41, 31, 29, 26, 23) // χ1, χ2, χ3, χ4, χ5
  private val PSI_PINS: List[Int] =
    List(43, 47, 51, 53, 59) // ψ1, ψ2, ψ3, ψ4, ψ5
  private val MU_PINS: List[Int] = List(37, 61) // μ1, μ2

  case class Wheel(pins: List[Int], pos: Int) {
    def rotate: Wheel = copy(pos = (pos + 1) % pins.size)

    def bit: Int = pins(pos)
  }

  object Wheel {
    def loadDefaultWheel(pins: Int): Wheel = {
      Wheel((0 until pins).toList.map(_ % 2), 0)
    }
  }

  case class LorenzState(
      mu1: Wheel,
      mu2: Wheel,
      chi: List[Wheel],
      psi: List[Wheel]
  ) {
    def rotateMu1: LorenzState = copy(mu1 = mu1.rotate)

    def rotateMu2: LorenzState = copy(mu2 = mu1.bit match {
      case 1 => mu2.rotate
      case _ => mu2
    })

    def rotateChi: LorenzState = copy(chi = chi.map(_.rotate))

    def rotatePsi: LorenzState = copy(psi = (mu1.bit, mu2.bit) match {
      case (a, b) if a != b => psi.map(_.rotate)
      case _                => psi
    })

    def updateState(): LorenzState = {
      rotateMu1.rotateChi.rotateMu2.rotatePsi
    }
  }

  /** Lorenz Machine action upon each key press
    *
    * 1. μ1 ∧ χ wheels turn
    * 2. μ2 turns ↔ μ1 == 1
    * 3. μ1 ⊕ μ2 == 1 → all ψ wheels are turned
    *    ~(μ1 ⊕ μ2 == 1) → ψ wheels don't turn
    */

  class LorenzMachine private (private val stateRef: Ref[LorenzState]) {
    def encipher(input: String): UIO[String] = {
      input.toList.foldLeft(ZIO.succeed("")) { (prev, char) =>
        for {
          state <- stateRef.get
          inputBits: Option[String] = getBits(char.toString)
          chi: String = state.chi.map(_.bit).mkString
          psi: String = state.psi.map(_.bit).mkString
          xorBits: String = bitwiseXOR(chi, psi)
          encipheredBits: String = bitwiseXOR(
            inputBits.getOrElse(""),
            xorBits
          )
          encipheredChar: String = getCharacter(encipheredBits).getOrElse("")
          _ <- stateRef.update(_.updateState())
          result <- prev.map(_ + encipheredChar)
        } yield result
      }
    }
  }

  object LorenzMachine {

    def createMachine(
        chiWheelsOpt: Option[List[Wheel]] = None,
        psiWheelsOpt: Option[List[Wheel]] = None,
        muWheelsOpt: Option[List[Wheel]] = None
    ): UIO[Either[String, LorenzMachine]] = {

      val createWheel
          : (Option[List[Wheel]], List[Int]) => Either[String, List[Wheel]] =
        (userWheelsOpt, defaultPins) =>
          userWheelsOpt match {
            case Some(userWheels) =>
              if (
                userWheels.length == defaultPins.length &&
                userWheels.zip(defaultPins).forall { case (wheel, pinCount) =>
                  wheel.pins.length == pinCount
                }
              )
                Right(userWheels)
              else
                Left("Incorrect wheel configuration provided.")
            case None =>
              Right(defaultPins.map(Wheel.loadDefaultWheel))
          }

      val chiWheels = createWheel(chiWheelsOpt, CHI_PINS)
      val psiWheels = createWheel(psiWheelsOpt, PSI_PINS)
      val muWheels = createWheel(muWheelsOpt, MU_PINS)

      (chiWheels, psiWheels, muWheels) match {
        case (Right(chi), Right(psi), Right(mu)) =>
          Ref
            .make(
              LorenzState(
                mu1 = mu.head,
                mu2 = mu.last,
                chi = chi,
                psi = psi
              )
            )
            .map(ref => Right(new LorenzMachine(ref)))
        case _ =>
          ZIO.succeed(
            Left(
              "Failed to create machine state due to incorrect wheel configuration."
            )
          )
      }
    }
  }
}
