package ca.cbotek.shared

/**
  * Utility class defining a Percentage class.
  *
  * In this representation, the percentage amount is bounded between zero and one.
  *
  * @param value value of the percentage.
  */
class Percentage(val value: Double) extends AnyVal {
  def complement = Percentage(1.0 - value)
}

object Percentage {

  /**
    * Apply method of a percentage.
    *
    * This method ensures that the percentage is always between zero and one included.
    *
    * @param value value of the percentage
    * @return A percentage between zero and one
    * @throws IllegalArgumentException if the requirement is not met.
    */
  def apply(value: Double): Percentage = {
    require(value >= 0.0 && value <= 1.0)
    new Percentage(value)
  }
}
