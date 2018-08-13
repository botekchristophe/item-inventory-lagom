package ca.cbotek.item.api.bundle

import java.util.UUID

import play.api.libs.json._

/**
  * A Bundle is a set of items which altogether have a different price than the items purchased one by one.
  *
  * @param id unique id of the bundle.
  * @param name unique name of the bundle.
  * @param items set of items defining this bundle.
  * @param price price of purchase of the whole bundle.
  * @param average_discount average discount on the bundle
  */
case class Bundle(id: UUID,
                  name: String,
                  items: Set[BundleItem],
                  price: Double,
                  average_discount: Double)

object Bundle {
  implicit val format: Format[Bundle] = Json.format[Bundle]

  /**
    * Helper apply method which computes a bundle price based on the set of items composing the bundle and a given
    * discount percentage.
    *
    * @param id bundle unique identifier.
    * @param name bundle name.
    * @param items bundle items along with their quantity.
    * @param average_discount discount percentage to apply to the set of items.
    * @return a Bundle with a price computed based on a given discount percentage and the bundle set of item.
    */
  def apply(id: UUID,
            name: String,
            items: Set[BundleItem],
            average_discount: Double): Bundle = {
    require(items.nonEmpty)
    Bundle(
      id = id,
      name = name,
      items = items,
      price = items.map(i => i.quantity * i.item.price).sum * (1 - average_discount),
      average_discount = average_discount)
  }
}
