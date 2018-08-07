package ca.cbotek.item.impl.entity

import java.util.UUID

import ca.cbotek.item.api.{Cart, CartItem}
import ca.cbotek.item.impl.command.{CheckoutCart, CreateCart, SetItemToCart}
import ca.cbotek.item.impl.event.{CartCheckedout, CartCreated, CartItemsUpdated}
import ca.cbotek.item.impl.model.{CartState, CartStatuses}

import scala.util.Random

object Mock {

  // Mock for Cart values
  final val cartId: UUID = UUID.randomUUID()
  final val cartUser: String = "user1"
  final val cartItem1: CartItem = CartItem(UUID.randomUUID(), Random.nextInt)
  final val cartItem2: CartItem = CartItem(UUID.randomUUID(), Random.nextInt)
  final val cartItems: Set[CartItem] = Set(cartItem1, cartItem2)
  final val setItemNewQuantity: Int = Random.nextInt
  final val cartItemsWithNewQuantity = Set(cartItem2, CartItem(cartItem1.itemId, setItemNewQuantity))
  final val checkoutPrice: Double = Random.nextDouble()

  // Mock for Cart states
  final val cartEmptyState: Option[CartState] = None
  final val cartInUseState: CartState = CartState(cartId, cartUser, cartItems, CartStatuses.IN_USE)
  final val cartInUseStateWithUpdate: CartState = cartInUseState.copy(items = cartItemsWithNewQuantity)
  final val cartCheckedoutState: CartState = cartInUseState.copy(status = CartStatuses.CHECKED_OUT)

  // Mock for Cart command, response and events
  final val createCartCommand: CreateCart = CreateCart(cartId, cartUser, cartItems)
  final val createCartResponse: Cart = Cart(cartId, cartUser, cartItems, CartStatuses.IN_USE.toString)
  final val cartCreated: CartCreated = CartCreated(cartId, cartUser, cartItems)

  final val setItemToCartCommand: SetItemToCart = SetItemToCart(cartId, cartItem1.itemId, setItemNewQuantity)
  final val setItemToCartResponse: Cart = Cart(cartId, cartUser, cartItemsWithNewQuantity, CartStatuses.IN_USE.toString)
  final val cartItemsUpdated: CartItemsUpdated = CartItemsUpdated(cartId, cartItemsWithNewQuantity)

  final val checkoutCartCommand: CheckoutCart = CheckoutCart(cartId, checkoutPrice)
  final val checkoutCartResponse: Cart = createCartResponse.copy(
    status = CartStatuses.CHECKED_OUT.toString,
    checkout_price = Some(checkoutPrice))
  final val cartCheckedout: CartCheckedout = CartCheckedout(cartId, checkoutPrice)
}
