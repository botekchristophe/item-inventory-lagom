package ca.cbotek.item.impl.entity

import ca.cbotek.item.api.{Cart, CartItem}
import ca.cbotek.item.impl.command.{CartCommand, CheckoutCart, CreateCart, SetItemToCart}
import ca.cbotek.item.impl.event.{CartCheckedout, CartCreated, CartEvent, CartItemsUpdated}
import ca.cbotek.item.impl.model.{CartState, CartStatuses}
import ca.cbotek.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.slf4j.LoggerFactory

class CartEntity extends PersistentEntity {

  override type Command = CartCommand[_]
  override type Event = CartEvent
  override type State = Option[CartState]

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  override def initialState: Option[CartState] = None

  var currentState: Option[CartState] = initialState

  private val log = LoggerFactory.getLogger(classOf[CartEntity])

  override def behavior: Behavior = {
    case None => unCreated
    case Some(cart) if cart.status == CartStatuses.IN_USE => inUse
    case Some(cart) if cart.status == CartStatuses.CHECKEDOUT => checkedOut
  }

  private def unCreated: Actions =
    Actions()
      .onCommand[CreateCart, Either[ErrorResponse, Cart]] { createCart }
      .onCommand[SetItemToCart, Either[ErrorResponse, Cart]] { replyNotFound }
      .onCommand[CheckoutCart, Either[ErrorResponse, Cart]] { replyNotFound }
      .onEvent {
        case (CartCreated(id, user, items), _) =>
          Some(CartState(id, user, items, CartStatuses.IN_USE))
        case (_, state) =>
          state
      }

  private def inUse: Actions =
    Actions()
      .onCommand[CreateCart, Either[ErrorResponse, Cart]] { replyConflict }
      .onCommand[SetItemToCart, Either[ErrorResponse, Cart]] { setItemToCart }
      .onCommand[CheckoutCart, Either[ErrorResponse, Cart]] { checkoutCart }
      .onEvent {
        case (CartItemsUpdated(_, updatedItems), cartState) =>
          cartState.map(cart => cart.copy(items = updatedItems))
        case (CartCheckedout(_), cartState) =>
          cartState.map(cart => cart.copy(status = CartStatuses.CHECKEDOUT))
        case (_, state) =>
          state
      }

  private def checkedOut: Actions =
    Actions()
      .onCommand[CreateCart, Either[ErrorResponse, Cart]] { replyConflict }
      .onCommand[SetItemToCart, Either[ErrorResponse, Cart]] { replyCartCheckedOut }
      .onCommand[CheckoutCart, Either[ErrorResponse, Cart]] { replyCartCheckedOut }
      .onEvent {
        case (_, state) =>
          state
      }

  private def createCart: OnCommandHandler[Either[ErrorResponse, Cart]] = {
    case (CreateCart(id, user, items), ctx, _) =>
      ctx.thenPersist(CartCreated(id, user, items))(_ => ctx.reply(Right(Cart(id, user, items))))
  }

  private def setItemToCart: OnCommandHandler[Either[ErrorResponse, Cart]] = {
    case (SetItemToCart(id, itemId, qtt), ctx, Some(cart)) =>
      val updatedItems: Set[CartItem] =
        cart
          .items
          .find(_.itemId == itemId)
          .fold(cart.items + CartItem(itemId, qtt))(item => cart.items.filterNot(_.itemId == itemId) + item.copy(quantity = qtt))
      ctx.thenPersist(CartItemsUpdated(id, updatedItems))(_ => ctx.reply(Right(Cart(id, cart.user, updatedItems))))
  }

  private def checkoutCart: OnCommandHandler[Either[ErrorResponse, Cart]] = {
    case (CheckoutCart(id), ctx, Some(cart)) =>
      ctx.thenPersist(CartCheckedout(id))(_ => ctx.reply(Right(Cart(id, cart.user, cart.items))))
  }

  private def replyNotFound[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(404, "Not found", "Cart not found.")))
      ctx.done
  }

  private def replyConflict[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(409, "Conflict", "Cart already exists.")))
      ctx.done
  }

  private def replyCartCheckedOut[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(400, "Bad Request", "Cart already checked-out.")))
      ctx.done
  }
}

