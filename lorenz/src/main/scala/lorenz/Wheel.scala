package lorenz

case class Wheel(
                // Indices of which pins are flipped on the wheel to register as a binary '1'.
                pin_settings: String,
                // As per Bill Tutte's nomenclature, you had χ, ψ, and μ wheels - to be implemented as chi, psi and mu.
                wheel_type: String,
                number_of_pins: Int,
                // Current pin the wheel is at.
                position: Int
                ) {

}
