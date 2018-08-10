package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, _}
import play.api.libs.json.{Reads, _}

/**
  * A user cat can be in two states, either 'IN_USE' or 'CHECKED_OUT'. Once the cart is checked out a user cannot add
  * more items in it.
  *
  * @param id unique id of the cart.
  * @param user unique identifier of the user owning the cart.
  * @param items set of items contained in the cart as well as the quantity per item.
  * @param status status of the cart.
  * @param checkout_price If the cart status is 'CHECKED_OUT', this field will hold the price at which this cart was optimized.
  */
case class Cart(id: UUID,
                user: String,
                items: Set[CartItem],
                status: String,
                checkout_price: Option[Double] = None)

object Cart {
  implicit val format: Format[Cart] = Json.format[Cart]
}

/**
  * A CartRequest is a representation of a cart in the perspective of a creation request. At this point the cart does not have
  * an id yet neither a status nor a checkout price.
  *
  * @param user user owning the cart. For simplicity sake, this is a simple [[String]] but for a more 'production' environment
  *             this would be a [[UUID]].
  * @param items Set of items and respective quantity per item contained in the cart.
  */
case class CartRequest(user: String,
                       items: Set[CartItem])

object CartRequest {
  implicit val format: Format[CartRequest] = Json.format[CartRequest]
}

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


/**
  * CartBundle is the representation of a Bundle from a Cart perspective. In this representation the bundle is represented
  * by its unique identifier and a quantity.
  *
  * @param bundleId bundle unique identifier.
  * @param quantity bundle quantity in this cart.
  */
case class CartBundle(bundleId: UUID,
                      quantity: Int)

object CartBundle {
  implicit val format: Format[CartBundle] = Json.format[CartBundle]
}

/**
  * A Cart Checkout is the representation of a Cart in a checkout context. In this specific context, the cart price might be
  * optimized using some bundle instead of separated items.
  *
  * @param id car unique identifier.
  * @param user user identifier.
  * @param price price of the cart after optimization.
  * @param items items part of this cart after optimization.
  * @param bundles bundles part of this cart after optimization.
  */
case class CartCheckout(id: UUID,
                        user: String,
                        price: Double,
                        items: Set[CartItem],
                        bundles: Set[CartBundle])

object CartCheckout {
  implicit val format: Format[CartCheckout] = Json.format[CartCheckout]
}