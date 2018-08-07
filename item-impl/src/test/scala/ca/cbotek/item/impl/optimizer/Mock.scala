package ca.cbotek.item.impl.optimizer

import java.util.UUID

import ca.cbotek.item.api._
import ca.cbotek.item.impl.model.ItemInventoryState

import scala.util.Random

object Mock {

  def randomQuantity: Int = Random.nextInt(100)
  //Creates a random double between 0.0 and 100
  def randomPrice: Double = (Random.nextDouble() + 1.0) * 100
  def randomItemName: String = Random.nextString(10)
  def randomItem: Item = Item(UUID.randomUUID(), randomItemName, "description", randomPrice)

  final val item1 = randomItem
  final val item2 = randomItem
  final val item3 = randomItem
  final val item4 = randomItem

  final val bundle1 = Bundle(Set(BundleItem(2, item1), BundleItem(5, item2)))
  final val bundle2 = Bundle(Set(BundleItem(1, item2), BundleItem(1, item1), BundleItem(1, item3)))
  final val bundle3 = Bundle(Set(BundleItem(5, item1)))


  final val cartItem1 = CartItem(item1.id, randomQuantity)
  final val cartItem2 = CartItem(item2.id, randomQuantity)
  final val cartItem3 = CartItem(item3.id, randomQuantity)
  final val cartItem4 = CartItem(item4.id, randomQuantity)
  final val cartItems = Set(cartItem1, cartItem2, cartItem3, cartItem4)
  final val nonOptimizedPrice =
    item1.price * cartItem1.quantity +
      item2.price * cartItem2.quantity +
      item3.price * cartItem3.quantity +
      item4.price * cartItem4.quantity

  final val inventory: ItemInventoryState =
    ItemInventoryState(
      Set(item1, item2, item3, item4),
      Set(bundle1, bundle2, bundle3))

  final def randomCart: CartCheckout =
    CartCheckout(
      id = UUID.randomUUID(),
      user = "user",
      price = nonOptimizedPrice,
      items = cartItems,
      bundles = Set.empty[CartBundle])

  final def emptyCart: CartCheckout =
    CartCheckout(
      id = UUID.randomUUID(),
      user = "user",
      price = 0.0,
      items = Set.empty[CartItem],
      bundles = Set.empty[CartBundle])

}
