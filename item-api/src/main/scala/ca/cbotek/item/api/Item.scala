package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

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

/**
  * An item representation in the context of a creation. At this stage, its unique identifier is not know yet.
  *
  * @param name an item unique name. It is important to note that the creation of an item will be rejected if another item
  *             already exist with the same name.
  * @param description the long description of an Item. This will be displayed to user when they request to see the Catalog.
  * @param price the default price of an item when it's not part of a bundle.
  */
case class ItemRequest(name: String,
                       description: String,
                       price: Double)

object ItemRequest {
  implicit val reads: Reads[ItemRequest] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").read[String](minLength[String](2)) and
      (JsPath \ "price").read[Double](min[Double](0.01))
    )(ItemRequest.apply _)

  implicit val writes: Writes[ItemRequest] = Json.writes[ItemRequest]
  implicit val format: Format[ItemRequest] = Format(reads, writes)
}
