package ca.cbotek.item.api.cart

import java.util.UUID

import play.api.libs.json._

/**
  * A CartCreateRequest is a representation of a cart in the perspective of a creation request. At this point the cart
  * does not have an id yet neither a status nor a checkout price.
  *
  * @param user user owning the cart. For simplicity sake, this is a simple [[String]] but for a more 'production'
  *             environment this would be a [[UUID]].
  * @param items Set of items and respective quantity per item contained in the cart.
  */
case class CartCreateRequest(user: String,
                             items: Set[CartItem],
                             bundles: Set[CartBundle])

object CartCreateRequest {
  implicit val format: Format[CartCreateRequest] = Json.format[CartCreateRequest]
}
