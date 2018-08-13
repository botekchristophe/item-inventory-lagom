package ca.cbotek.item.api.cart

import play.api.libs.json._

/**
  * A CartUpdateRequest is a representation of a cart in the perspective of an update request.
  *
  * @param items Set of items and respective quantity per item to set in the cart.
  * @param bundles Set of bundles and respective quantity per bundle to set in the cart.
  */
case class CartUpdateRequest(items: Set[CartItem],
                             bundles: Set[CartBundle])

object CartUpdateRequest {
  implicit val format: Format[CartUpdateRequest] = Json.format[CartUpdateRequest]
}
