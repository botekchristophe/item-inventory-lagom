package ca.cbotek.item.api.bundle

import ca.cbotek.item.api.item.Item
import play.api.libs.json._

/**
  * A bundleItem is an [[Item]] and a quantity representing the number of items being purchased while purchasing the
  * bundle.
  *
  * @param quantity number of items being part of the bundle.
  * @param item type of item being part of the bundle.
  */
case class BundleItem(quantity: Int,
                      item: Item)

object BundleItem {
  implicit val format: Format[BundleItem] = Json.format[BundleItem]
}
