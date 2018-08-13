package ca.cbotek.item.api.cart

import java.util.UUID

import play.api.libs.json._

/**
  * A user Cart
  *
  * Once the cart is checked out a user cannot update the cart anymore.
  *
  * @param id unique id of the cart.
  * @param user unique identifier of the user owning the cart.
  * @param items set of items selected in the user's cart as well as item quantities.
  * @param bundles set of bundles selected in the user's cart as well as bundle quantities.
  * @param price price of the current items and bundles in the cart. None if the price remains to be computed
  */
case class Cart(id: UUID,
                user: String,
                items: Set[CartItem],
                bundles: Set[CartBundle],
                price: Option[Double] = None)

object Cart {
  implicit val format: Format[Cart] = Json.format[Cart]
}
