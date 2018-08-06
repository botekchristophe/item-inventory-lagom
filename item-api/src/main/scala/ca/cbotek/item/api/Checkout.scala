package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class Checkout(id: UUID,
                    best_price: Double,
                    price_combinations: Set[CheckoutCombination])

object Checkout {
  implicit val format: Format[Checkout] = Json.format[Checkout]
}

case class CheckoutCombination(price: Double,
                               items: Iterable[CheckoutItem],
                               bundles: Iterable[CheckoutBundle])

object CheckoutCombination {
  implicit val format: Format[CheckoutCombination] = Json.format[CheckoutCombination]
}

case class CheckoutItem(quantity: Int,
                        itemId: UUID)

object CheckoutItem {
  implicit val format: Format[CheckoutItem] = Json.format[CheckoutItem]
}

case class CheckoutBundle(quantity: Int,
                          bundleId: UUID)

object CheckoutBundle {
  implicit val format: Format[CheckoutBundle] = Json.format[CheckoutBundle]
}
