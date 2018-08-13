package ca.cbotek.item.api.item

import java.util.UUID

import play.api.libs.json._

/**
  * An Item is a object that can be purchased by a user and is available in the Catalog. A user can add items to its
  * Cart and request a checkout action that will optimize the user's Cart price and might group some items into bundles
  * in order to drop the price down.
  *
  * @param id an item unique identifier
  * @param name an item unique name
  * @param description an item description
  * @param price an item default price if it's not part of a bundle.
  */
case class Item(id: UUID,
                name: String,
                description: String,
                price: Double)

object Item {
  implicit val format: Format[Item] = Json.format[Item]
}
