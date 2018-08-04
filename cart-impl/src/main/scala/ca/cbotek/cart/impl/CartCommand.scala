package ca.cbotek.cart.impl

import java.util.UUID

import ca.cbotek.cart.api.{Cart, CartItem}
import ca.cbotek.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait CartCommand[R] extends ReplyType[R]

case class CreateCart(id: UUID,
                      user: String,
                      items: Set[CartItem]) extends CartCommand[Either[ErrorResponse, Cart]]
object CreateCart {
  implicit val format: Format[CreateCart] = Json.format[CreateCart]
}

case class SetItemToCart(id: UUID, itemId: UUID, quantity: Int) extends CartCommand[Either[ErrorResponse, Cart]]
object SetItemToCart {
  implicit val format: Format[SetItemToCart] = Json.format[SetItemToCart]
}

case class CheckoutCart(id: UUID) extends CartCommand[Either[ErrorResponse, Cart]]
object CheckoutCart {
  implicit val format: Format[CheckoutCart] = Json.format[CheckoutCart]
}
