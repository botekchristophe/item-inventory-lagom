package ca.cbotek.item.impl.command

import java.util.UUID

import ca.cbotek.item.api.cart.{Cart, CartBundle, CartItem}
import ca.cbotek.item.impl.ServiceErrors.ServiceError
import ca.cbotek.shared.JsonFormats.singletonFormat
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

/**
  * Cart command are case classes or case object that can be sent to a [[ca.cbotek.item.impl.entity.CartEntity]] as a message.
  * A command response type has to be defined in order for the calling process to know what type can be inferred from calling
  * the entity.
  * @tparam R response type of the message.
  */
sealed trait CartCommand[R] extends ReplyType[R]

/**
  * This command will send a Create message to the entity.
  *
  * Command is invalid if the cart already exists.
  *
  * Will persist an event [[ca.cbotek.item.impl.event.CartCreated]] if the command is accepted.
  *
  * @param id id the the cart.
  * @param user user owning the cart.
  * @param items items added to the cart.
  * @param bundles bundles added to the cart.
  */
case class CreateCart(id: UUID,
                      user: String,
                      items: Set[CartItem],
                      bundles: Set[CartBundle]) extends CartCommand[Either[ServiceError, Cart]]
object CreateCart {
  implicit val format: Format[CreateCart] = Json.format[CreateCart]
}

/**
  * This command will set a new set of items and bundles for the cart.
  *
  * Command is invalid if the cart does not exists or if the cart is already checked out.
  *
  * Will persist an event [[ca.cbotek.item.impl.event.CartUpdated]] if the command is accepted.
  *
  * @param id unique identifier of the cart.
  * @param items new set of items for the cart.
  * @param bundles new set of bundles for the cart.
  */
case class UpdateCart(id: UUID,
                      items: Set[CartItem],
                      bundles: Set[CartBundle]) extends CartCommand[Either[ServiceError, Cart]]
object UpdateCart {
  implicit val format: Format[UpdateCart] = Json.format[UpdateCart]
}

/**
  * This command will set a cart status to 'CHECKED_OUT' if the command is valid.
  *
  * Command is invalid if the cart does not exists or if the cart is already checked out.
  *
  * Will persist an event [[ca.cbotek.item.impl.event.CartCheckedOut]] if the command is accepted.
  *
  * @param id unique identifier of the cart.
  */
case class CheckoutCart(id: UUID) extends CartCommand[Either[ServiceError, Cart]]
object CheckoutCart {
  implicit val format: Format[CheckoutCart] = Json.format[CheckoutCart]
}

/**
  * This command will return the state of a cart if it's valid.
  *
  * Command is invalid if the cart does not exists or if the cart is already checked out.
  *
  * Will not persist any events
  */
case object GetOneCart extends CartCommand[Either[ServiceError, Cart]] {
  implicit val format: Format[GetOneCart.type] = singletonFormat(GetOneCart)
}

