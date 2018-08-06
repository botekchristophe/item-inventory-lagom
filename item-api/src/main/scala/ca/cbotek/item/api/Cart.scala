package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, _}
import play.api.libs.json.{Reads, _}

case class Cart(id: UUID,
                user: String, //user identifier
                items: Set[CartItem],
                status: String,
                checkout_price: Option[Double] = None)

object Cart {
  implicit val format: Format[Cart] = Json.format[Cart]
}

case class CartRequest(user: String,
                       items: Set[CartItem])

object CartRequest {
  implicit val format: Format[CartRequest] = Json.format[CartRequest]
}

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


case class CartBundle(bundleId: UUID,
                      quantity: Int)

object CartBundle {
  implicit val format: Format[CartBundle] = Json.format[CartBundle]
}

case class CartCheckout(id: UUID,
                        user: String,
                        price: Double,
                        items: Set[CartItem],
                        bundles: Set[CartBundle])

object CartCheckout {
  implicit val format: Format[CartCheckout] = Json.format[CartCheckout]
}