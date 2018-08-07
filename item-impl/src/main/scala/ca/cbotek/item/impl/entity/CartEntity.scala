package ca.cbotek.item.impl.entity

import ca.cbotek.item.api.{Cart, CartItem}
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event.{CartCheckedout, CartCreated, CartEvent, CartItemsUpdated}
import ca.cbotek.item.impl.model.{CartState, CartStatuses}
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
    case Some(cart) if cart.status == CartStatuses.CHECKED_OUT => checkedOut
  }

  private def unCreated: Actions =
    Actions()
      .onCommand[CreateCart, Either[ServiceError, Cart]] { createCart }
      .onCommand[SetItemToCart, Either[ServiceError, Cart]] { replyNotFound }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { replyNotFound }
      .onCommand[GetOneCart.type, Either[ServiceError, Cart]] { replyNotFound }
      .onEvent {
        case (CartCreated(id, user, items), _) =>
          Some(CartState(id, user, items, CartStatuses.IN_USE))
        case (_, state) =>
          state
      }

  private def inUse: Actions =
    Actions()
      .onCommand[CreateCart, Either[ServiceError, Cart]] { replyConflict }
      .onCommand[SetItemToCart, Either[ServiceError, Cart]] { setItemToCart }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { checkoutCart }
      .onReadOnlyCommand[GetOneCart.type, Either[ServiceError, Cart]] { getCart }
      .onEvent {
        case (CartItemsUpdated(_, updatedItems), cartState) =>
          cartState.map(cart => cart.copy(items = updatedItems))
        case (CartCheckedout(_, _), cartState) =>
          cartState.map(cart => cart.copy(status = CartStatuses.CHECKED_OUT))
        case (_, state) =>
          state
      }

  private def checkedOut: Actions =
    Actions()
      .onCommand[CreateCart, Either[ServiceError, Cart]] { replyConflict }
      .onCommand[SetItemToCart, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onCommand[GetOneCart.type, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onEvent {
        case (_, state) =>
          state
      }

  private def createCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (CreateCart(id, user, items), ctx, _) =>
      ctx.thenPersist(CartCreated(id, user, items))(_ => ctx.reply(Right(Cart(id, user, items, CartStatuses.IN_USE.toString))))
  }

  private def setItemToCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (SetItemToCart(id, itemId, qtt), ctx, Some(cart)) =>
      val updatedItems: Set[CartItem] =
        cart
          .items
          .find(_.itemId == itemId)
          .fold(cart.items + CartItem(itemId, qtt))(item => cart.items.filterNot(_.itemId == itemId) + item.copy(quantity = qtt))
      ctx.thenPersist(CartItemsUpdated(id, updatedItems))(_ =>
        ctx.reply(Right(Cart(id, cart.user, updatedItems, CartStatuses.IN_USE.toString))))
  }

  private def checkoutCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (CheckoutCart(id, price), ctx, Some(cart)) =>
      ctx.thenPersist(CartCheckedout(id, price))(_ =>
        ctx.reply(Right(Cart(id, cart.user, cart.items, CartStatuses.CHECKED_OUT.toString, Some(price)))))
  }

  private def getCart: ReadOnlyHandler[Either[ServiceError, Cart]] = {
    case (GetOneCart, ctx, Some(c)) =>
      ctx.reply(Right(Cart(c.id, c.user, c.items, c.status.toString)))
  }

  private def replyNotFound[R]: OnCommandHandler[Either[ServiceError, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(CartNotFound))
      ctx.done
  }

  private def replyConflict[R]: OnCommandHandler[Either[ServiceError, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(CartConflict))
      ctx.done
  }

  private def replyCartCheckedOut[R]: OnCommandHandler[Either[ServiceError, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(CartCheckedOut))
      ctx.done
  }
}

