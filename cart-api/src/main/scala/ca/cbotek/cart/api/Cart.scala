package ca.cbotek.cart.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, _}
import play.api.libs.json.{Reads, _}

case class Cart(id: UUID,
                user: String, //user identifier
                expiry: Long,
                items: Set[CartItem])

object Cart {
  implicit val format: Format[Cart] = Json.format[Cart]
}

case class CartRequest(user: String,
                       items: Set[CartItem])

object CartRequest {
  implicit val format: Format[CartRequest] = Json.format[CartRequest]
}

case class CartItem(itemId: UUID,
                    itemName: String,
                    itemQuantity: Int)

object CartItem {
  implicit val format: Format[CartItem] = Json.format[CartItem]
}

case class AddItem(itemId: UUID,
                   quantity: Int)

object AddItem {
  implicit val reads: Reads[AddItem] = (
    (JsPath \ "itemId").read[UUID] and
      (JsPath \ "quantity").read[Int](min(1))
    )(AddItem.apply _)

  implicit val writes: Writes[AddItem] = Json.writes[AddItem]
  implicit val format: Format[AddItem] = Format(reads, writes)
}
