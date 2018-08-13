package ca.cbotek.item.api.cart

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, _}
import play.api.libs.json.{Reads, _}

/**
  * A CartItem is the representation of an item and a quantity per item from a cart perspective. The quantity of an item
  * has to be greater or equals to 1.
  *
  * @param itemId item unique identifier of an item in the whole application.
  * @param quantity quantity of this item. The value has to be greater or equals to 1.
  */
case class CartItem(itemId: UUID,
                    quantity: Int)
object CartItem {
  implicit val reads: Reads[CartItem] = (
    (JsPath \ "itemId").read[UUID] and
      (JsPath \ "quantity").read[Int](min(1))
    )(CartItem.apply _)

  implicit val writes: Writes[CartItem] = Json.writes[CartItem]
  implicit val format: Format[CartItem] = Format(reads, writes)
}
