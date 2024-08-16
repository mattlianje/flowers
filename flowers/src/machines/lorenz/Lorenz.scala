package machines.lorenz

import scala.util.{Try, Success, Failure}
import cats.data.State
import cats.implicits._
import commons._

case class Lorenz(
    mu1: Wheel,
    mu2: Wheel,
    chi: List[Wheel],
    psi: List[Wheel],
    shiftMode: ShiftMode
) extends Machine {

  /** Lorenz Machine action upon each key press
    *
    *   1. 1 step ∀ 𝝁1 ∧ 𝝌 
    *   2. 1 step 𝝁2 ↔ 𝝁1 == 1 
    *   3. 𝝁1 ⊕ 𝝁2 == 1 → ∀ 𝜓 1 step ¬(𝝁1 ⊕ 𝝁2 == 1) → ∀ 𝜓 0 step
    */
  private def updateState(): Lorenz = {
    val newMu1 = mu1.rotate
    val newMu2 = if (mu1.bit == 1) mu2.rotate else mu2

    val newChi = chi.map(_.rotate)
    val newPsi = if ((mu1.bit ^ mu2.bit) == 1) psi.map(_.rotate) else psi

    copy(mu1 = newMu1, mu2 = newMu2, chi = newChi, psi = newPsi)
  }

  private def getWheelBaudot(wheels: List[Wheel]): Baudot = {
    val bits = wheels.map(_.bit).mkString
    Baudot(bits).getOrElse(
      throw new IllegalArgumentException("Invalid Baudot from wheels")
    )
  }

  private def encodeBaudot(baudot: Baudot): State[Lorenz, Baudot] = State {
    machine =>
      val chiBaudot = getWheelBaudot(machine.chi)
      val psiBaudot = getWheelBaudot(machine.psi)

      val encryptedBaudot = baudot.xor(chiBaudot).xor(psiBaudot)

      val updatedMachine = machine.updateState()
      (updatedMachine, encryptedBaudot)
  }

  private def encodeBaudots(
      baudots: List[Baudot]
  ): State[Lorenz, List[Baudot]] = {
    baudots.traverse(encodeBaudot)
  }

  private def updateShiftMode(
      token: BaudotToken,
      currentMode: ShiftMode
  ): ShiftMode = {
    token match {
      case LetterShiftToken => LetterShift
      case FigureShiftToken => FigureShift
      case _                => currentMode
    }
  }

  def encrypt(input: String): Try[String] = {
    val tokensTry = Baudot.tokenize(input)
    tokensTry match {
      case Success(tokens) =>
        val baudots = tokens.flatMap(token => Baudot.tokenToBaudot(token))
        val encryptedBaudots = encodeBaudots(baudots).runA(this).value

        val (finalTokens, finalMode) =
          encryptedBaudots.foldLeft((List.empty[BaudotToken], shiftMode)) {
            case ((accTokens, currentMode), baudot) =>
              val token = baudot.getBaudotToken(currentMode)
              val newMode = updateShiftMode(token, currentMode)
              (accTokens :+ token, newMode)
          }

        Success(Baudot.tokensToString(finalTokens))

      case Failure(exception) =>
        Failure(
          new IllegalArgumentException(
            s"Failed to tokenize input string: $exception"
          )
        )
    }
  }
}
