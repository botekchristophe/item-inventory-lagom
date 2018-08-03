package ca.cbotek.cart.impl

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class CartState(id: UUID,
                     user: String)
object CartState {
  implicit val format: Format[CartState] = Json.format[CartState]
}
