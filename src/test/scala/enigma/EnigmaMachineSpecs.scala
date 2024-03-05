package enigma

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class EnigmaMachineSpecs extends AnyFlatSpec with Matchers {

  it should "correctly advance rotors after each key press" in {
    val rotor1 = Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'A', 'Q', 'A', "I")
    val rotor2 = Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'A', 'E', 'A', "II")
    val rotor3 = Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'A', 'V', 'A', "III")
    val reflector = Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
    val plugboard = Plugboard(Map.empty)
    val enigmaMachine =
      EnigmaMachine(List(rotor1, rotor2, rotor3), reflector, plugboard)

    val advancedMachine = enigmaMachine.advanceRotors
    advancedMachine.rotors(1).position shouldBe 'A'
    advancedMachine.rotors(2).position shouldBe 'A'
  }

  it should "encrypt and decrypt a message correctly" in {
    val rotor1 = Rotor("EKMFLGDQVZNTOWYHXUSPAIBRCJ", 'A', 'Q', 'A', "I")
    val rotor2 = Rotor("AJDKSIRUXBLHWTMCQGZNPYFVOE", 'A', 'E', 'A', "II")
    val rotor3 = Rotor("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'A', 'V', 'A', "III")
    val reflector = Reflector("YRUHQSLDPXNGOKMIEBFZCWVJAT")
    val plugboard = Plugboard(Map.empty)
    val enigmaMachine =
      EnigmaMachine(List(rotor1, rotor2, rotor3), reflector, plugboard)

    val encryptedMessage = enigmaMachine.encryptMessage("HELLOWORLD")
    encryptedMessage shouldBe Right("EHPPKMKIPU")
    enigmaMachine.encryptMessage("EHPPKMKIPU") shouldBe Right("HELLOWORLD")
  }
}
