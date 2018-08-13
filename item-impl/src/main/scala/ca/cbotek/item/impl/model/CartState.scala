package ca.cbotek.item.impl.model

import java.util.UUID

import ca.cbotek.item.api.cart.{CartBundle, CartItem}
import ca.cbotek.item.impl.model.CartStatuses.CartStatus
import ca.cbotek.shared.JsonFormats._
import play.api.libs.json.{Format, Json}

/**
  * A Cart state
  *
  * This class holds the state of a [[ca.cbotek.item.impl.entity.CartEntity]] when the state is defined.
  *
  * @param id unique identifier for a cart
  * @param user user owning the cart
  * @param items set of items added to this cart
  * @param bundles set of bundles added to this cart
  * @param status status of the cart. Can be one of [[CartStatuses.IN_USE]] or [[CartStatuses.CHECKED_OUT]]
  */
case class CartState(id: UUID,
                     user: String,
                     items: Set[CartItem],
                     bundles: Set[CartBundle],
                     status: CartStatus)
object CartState {
  implicit val format: Format[CartState] = Json.format[CartState]
}

/**
  * Enumeration holding the possible state of a cart.
  */
object CartStatuses extends Enumeration {
  type CartStatus = Value
  val IN_USE, CHECKED_OUT = Value

  implicit val format: Format[CartStatus] = enumFormat(CartStatuses)
}
