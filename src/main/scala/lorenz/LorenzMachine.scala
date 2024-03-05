package lorenz

import commons.Baudot.{
  BitString,
  baudotChunksToString,
  bitwiseXOR,
  stringToBaudotChunks
}

/** Case class representing the current state of a Lorenz machine and its wheels
  * By default it simulates an Sz-42A which had the psi wheels rotate under direction
  * of the mu wheels ... to remove the effect of the mu wheels set all their pins to `1`
  * or `0` and you can simulate Sz-40.
  *
  * @param mu1 : Wheel
  * @param mu2: Wheel
  * @param chi: List[Wheel]
  * @param psi: List[Wheel]
  */
case class LorenzMachine(
    mu1: Wheel,
    mu2: Wheel,
    chi: List[Wheel],
    psi: List[Wheel]
) {

  /** Lorenz Machine action upon each key press
    *
    * 1. 1 step ∀ 𝝁1 ∧ 𝝌
    * 2. 1 step 𝝁2  ↔ 𝝁1 == 1
    * 3. 𝝁1 ⊕ 𝝁2 == 1 → ∀ 𝜓 1 step
    * ¬(𝝁1 ⊕ 𝝁2 == 1) → ∀ 𝜓 0 step
    */
  def updateState(): LorenzMachine = {
    mu1.bit match {
      case 1 =>
        mu1.bit ^ mu2.bit match {
          case 1 =>
            copy(
              chi = chi.map(_.rotate),
              psi = psi.map(_.rotate),
              mu1 = mu1.rotate,
              mu2 = mu2.rotate
            )

          case _ =>
            copy(chi = chi.map(_.rotate), mu1 = mu1.rotate, mu2 = mu2.rotate)

        }
      case _ =>
        mu1.bit ^ mu2.bit match {
          case 1 =>
            copy(
              chi = chi.map(_.rotate),
              psi = psi.map(_.rotate),
              mu1 = mu1.rotate
            )

          case _ =>
            copy(chi = chi.map(_.rotate), mu1 = mu1.rotate)
        }
    }
  }

  /** @param input Plaintext Seq of BitString's P = Seq("10001", "00000")
    * @return Encrypted bitstrings Z = Seq("00011", "10100") where len(Z) = len(P)
    */
  def encipher(
      input: Seq[BitString]
  ): Either[RuntimeException, Seq[BitString]] = {
    input
      .foldLeft(
        Right((Seq.empty[BitString], this)): Either[
          RuntimeException,
          (Seq[BitString], LorenzMachine)
        ]
      ) {
        case (Right((prev, machine)), inputBits) =>
          val chi = machine.chi.map(_.bit).mkString
          val psi = machine.psi.map(_.bit).mkString

          bitwiseXOR(chi, psi) match {
            case Some(xorBits) =>
              bitwiseXOR(inputBits, xorBits) match {
                case Some(encipheredBits) =>
                  val updatedMachine = machine.updateState()
                  Right(prev :+ encipheredBits, updatedMachine)
                case None =>
                  Left(
                    new RuntimeException(
                      s"Failed to xor input bits with xorBits."
                    )
                  )
              }
            case None =>
              Left(new RuntimeException("Failed to xor chi and psi."))
          }
        case (left, _) => left
      }
      .map(_._1)
  }

  def encipherText(input: String): Either[RuntimeException, String] = {
    stringToBaudotChunks(input) match {
      case Some(bitStrings) =>
        encipher(bitStrings) match {
          case Right(encipheredBitStrings) =>
            baudotChunksToString(encipheredBitStrings.toList) match {
              case Some(result) => Right(result)
              case None =>
                Left(
                  new RuntimeException(
                    "Failed to convert enciphered bitstrings back to text."
                  )
                )
            }
          case Left(e) => Left(e)
        }
      case None =>
        Left(
          new RuntimeException(
            "Failed to convert input text to Baudot bitstrings."
          )
        )
    }
  }

}

object LorenzMachine {

  val CHI_PINS: List[Int] =
    List(41, 31, 29, 26, 23) // 𝝌1, ..., 𝝌5
  val PSI_PINS: List[Int] =
    List(43, 47, 51, 53, 59) // 𝜓1, ..., 𝜓5
  val MU_PINS: List[Int] = List(37, 61) // 𝝁1, 𝝁2

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
        case Some(userWheels) if validConfiguration(userWheels, defaultPins) =>
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
    } yield LorenzMachine(mu.head, mu.last, chi, psi)
  }
}
