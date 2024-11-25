package flowers

import scala.util.{Try, Success, Failure}
import cats.data.State
import cats.implicits._
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._
import io.circe.parser.decode

case class Lorenz(
                   mu1: Wheel,
                   mu2: Wheel,
                   chi: List[Wheel],
                   psi: List[Wheel],
                   shiftMode: ShiftMode
                 ) extends Machine {

  /** Lorenz Machine action upon each key press
   *
   *   1. 1 step ∀ 𝝁1 ∧ 𝝌(1-5)
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

  private def encodeBaudots(
                             baudots: List[Baudot]
                           ): State[Lorenz, List[Baudot]] = {
    def toBaudot(wheels: List[Wheel]): Option[Baudot] =
      Baudot(wheels.map(_.bit).mkString)

    def encode(b: Baudot): State[Lorenz, Baudot] = State { machine =>
      val encrypted = for {
        chi <- toBaudot(machine.chi)
        psi <- toBaudot(machine.psi)
      } yield b.xor(chi).xor(psi)

      (
        machine.updateState(),
        encrypted.getOrElse(
          throw new IllegalArgumentException("Invalid Baudot from wheels")
        )
      )
    }
    baudots.traverse(encode)
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

  def encryptStream(input: LazyList[String]): LazyList[Try[String]] = ???
}

object Lorenz {
  case class WheelConfig(
                          pins: List[Int],
                          position: Int
                        )

  case class LorenzConfig(
                           mu1: WheelConfig,
                           mu2: WheelConfig,
                           chi: List[WheelConfig],
                           /* Chi and Psi MUST have 5 wheels: TODO - handle better */
                           psi: List[WheelConfig],
                           shiftMode: String /* "LetterShift" or "FigureShift" */
                         )

  implicit val wheelConfigDecoder: Decoder[WheelConfig] = deriveDecoder
  implicit val lorenzConfigDecoder: Decoder[LorenzConfig] = deriveDecoder

  def fromConfig(jsonConfig: String): Try[Lorenz] = {
    decode[LorenzConfig](jsonConfig).toTry.flatMap { config =>
      Try {
        require(config.chi.length == 5, "Must have exactly 5 Chi wheels")
        require(config.psi.length == 5, "Must have exactly 5 Psi wheels")

        val mu1 = Wheel(config.mu1.pins, config.mu1.position)
        val mu2 = Wheel(config.mu2.pins, config.mu2.position)
        val chi = config.chi.map(w => Wheel(w.pins, w.position))
        val psi = config.psi.map(w => Wheel(w.pins, w.position))

        val shiftMode = config.shiftMode match {
          case "LetterShift" => LetterShift
          case "FigureShift" => FigureShift
          case m =>
            throw new IllegalArgumentException(s"Invalid shift mode: $m")
        }

        Lorenz(mu1, mu2, chi, psi, shiftMode)
      }
    }
  }
}

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
