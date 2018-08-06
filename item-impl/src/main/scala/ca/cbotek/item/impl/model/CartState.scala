package ca.cbotek.item.impl.model

import java.util.UUID

import ca.cbotek.item.api.CartItem
import ca.cbotek.item.impl.model.CartStatuses.CartStatus
import ca.cbotek.shared.JsonFormats._
import play.api.libs.json.{Format, Json}

case class CartState(id: UUID,
                     user: String,
                     items: Set[CartItem],
                     status: CartStatus)
object CartState {
  implicit val format: Format[CartState] = Json.format[CartState]
}

object CartStatuses extends Enumeration {
  type CartStatus = Value
  val IN_USE, CHECKED_OUT = Value

  implicit val format: Format[CartStatus] = enumFormat(CartStatuses)
}
