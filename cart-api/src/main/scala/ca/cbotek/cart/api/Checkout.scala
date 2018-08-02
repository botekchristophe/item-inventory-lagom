package ca.cbotek.cart.api

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class Checkout(id: UUID,
                    best_price: Double,
                    price_combinations: Set[CheckoutCombination])

object Checkout {
  implicit val format: Format[Checkout] = Json.format[Checkout]
}

case class CheckoutCombination(price: Double,
                               items: Map[String, Int],
                               bundles: Map[String, Int])

object CheckoutCombination {
  implicit val format: Format[CheckoutCombination] = Json.format[CheckoutCombination]
}
