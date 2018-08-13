package ca.cbotek.item.impl.entity

import ca.cbotek.item.api.cart.Cart
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event.{CartCheckedOut, CartCreated, CartEvent, CartUpdated}
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
      .onCommand[UpdateCart, Either[ServiceError, Cart]] { replyNotFound }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { replyNotFound }
      .onCommand[GetOneCart.type, Either[ServiceError, Cart]] { replyNotFound }
      .onEvent {
        case (CartCreated(id, user, items, bundles), _) =>
          Some(CartState(id, user, items, bundles, CartStatuses.IN_USE))
        case (_, state) =>
          state
      }

  private def inUse: Actions =
    Actions()
      .onCommand[CreateCart, Either[ServiceError, Cart]] { replyConflict }
      .onCommand[UpdateCart, Either[ServiceError, Cart]] { updateCart }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { checkoutCart }
      .onReadOnlyCommand[GetOneCart.type, Either[ServiceError, Cart]] { getCart }
      .onEvent {
        case (CartUpdated(_, updatedItems, updatedBundles), cartState) =>
          cartState.map(cart => cart.copy(items = updatedItems, bundles = updatedBundles))
        case (CartCheckedOut(_), cartState) =>
          cartState.map(cart => cart.copy(status = CartStatuses.CHECKED_OUT))
        case (_, state) =>
          state
      }

  private def checkedOut: Actions =
    Actions()
      .onCommand[CreateCart, Either[ServiceError, Cart]] { replyConflict }
      .onCommand[UpdateCart, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onCommand[CheckoutCart, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onCommand[GetOneCart.type, Either[ServiceError, Cart]] { replyCartCheckedOut }
      .onEvent {
        case (_, state) =>
          state
      }

  private def createCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (CreateCart(id, user, items, bundles), ctx, _) =>
      ctx.thenPersist(CartCreated(id, user, items, bundles))(_ => ctx.reply(Right(Cart(id, user, items, bundles))))
  }

  private def updateCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (UpdateCart(id, items, bundles), ctx, Some(cart)) =>
      ctx.thenPersist(CartUpdated(id, items, bundles))(_ => ctx.reply(Right(Cart(id, cart.user, items, bundles))))
  }

  private def checkoutCart: OnCommandHandler[Either[ServiceError, Cart]] = {
    case (CheckoutCart(id), ctx, Some(cart)) =>
      ctx.thenPersist(CartCheckedOut(id))(_ =>
        ctx.reply(Right(Cart(id, cart.user, cart.items, cart.bundles))))
  }

  private def getCart: ReadOnlyHandler[Either[ServiceError, Cart]] = {
    case (GetOneCart, ctx, Some(c)) =>
      ctx.reply(Right(Cart(c.id, c.user, c.items, c.bundles)))
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
      ctx.reply(Left(CartCannotBeUpdated))
      ctx.done
  }
}

