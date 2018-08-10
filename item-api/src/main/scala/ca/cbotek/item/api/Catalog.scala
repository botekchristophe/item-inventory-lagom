package ca.cbotek.item.api

import play.api.libs.json._

/**
  * A Catalog is the representation of the item inventory from a user perspective. From the user, items and bundles do not
  * have a stock or a quantity associated to them. This shows to the user, all the items and bundle available in the inventory.
  *
  * @param items set of items available in the inventory
  * @param bundles set of bundle available in the inventory
  */
case class Catalog(items: Set[Item],
                   bundles: Set[Bundle])

object Catalog {
  implicit val format: Format[Catalog] = Json.format[Catalog]
}
