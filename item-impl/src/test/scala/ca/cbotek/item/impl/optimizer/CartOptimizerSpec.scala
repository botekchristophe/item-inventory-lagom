package ca.cbotek.item.impl.optimizer

import java.util.UUID

import ca.cbotek.item.api.bundle.{Bundle, BundleItem}
import ca.cbotek.item.api.cart.{Cart, CartBundle, CartItem}
import ca.cbotek.item.api.item.Item
import ca.cbotek.item.api.{bundle, cart, _}
import ca.cbotek.item.impl.model.ItemInventoryState
import org.scalatest.{Matchers, WordSpec}
import akka.actor.ActorSystem
import akka.testkit.TestKit
import ca.cbotek.item.api.bundle.{Bundle, BundleItem, BundleRequestItem}
import ca.cbotek.item.api.item.Item
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event._
import ca.cbotek.item.impl.model.ItemInventoryState
import ca.cbotek.item.impl.{ItemSerializerRegistry, ServiceErrors}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.util.Random

object Mock {

  def randomQuantity: Int = Random.nextInt(100)
  def randomPrice: Double = (Random.nextDouble() + 1.0) * 100
  def randomItemName: String = Random.nextString(10)
  def randomItem: Item = Item(UUID.randomUUID(), randomItemName, "description", randomPrice)

  final val item1 = randomItem
  final val item2 = randomItem
  final val item3 = randomItem
  final val item4 = randomItem

  final val bundle1 = Bundle(UUID.randomUUID(), "b1", Set(BundleItem(2, item1), BundleItem(5, item2)), 0.1)
  final val bundle2 = Bundle(UUID.randomUUID(), "b2",Set(BundleItem(1, item2), BundleItem(1, item1), BundleItem(1, item3)), 0.2)
  final val bundle3 = Bundle(UUID.randomUUID(), "b3",Set(BundleItem(5, item1)), 0.3)


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

  final def randomCart: Cart =
    Cart(
      id = UUID.randomUUID(),
      user = "user",
      items = cartItems,
      bundles = Set.empty[CartBundle],
      price = Some(nonOptimizedPrice))

  final def emptyCart: Cart =
    Cart(
      id = UUID.randomUUID(),
      user = "user",
      items = Set.empty[CartItem],
      bundles = Set.empty[CartBundle],
      price = Some(0.0))
}

class CartOptimizerSpec extends WordSpec with Matchers {

  "Cart optimizer" should {
    "Compute cart with random items price" in {
      CartOptimizer.computeCartPrice(Mock.randomCart, Mock.inventory).price.contains(Mock.nonOptimizedPrice) shouldBe true
    }

    "Compute empty cart" in {
      CartOptimizer.computeCartPrice(Mock.emptyCart, Mock.inventory).price.contains(0.0) shouldBe true
    }

    "Find an optimized price smaller than starting price" in {
      val optimizedCart = CartOptimizer.optimizeCart(Mock.randomCart, Mock.inventory.bundles)
        CartOptimizer.computeCartPrice(optimizedCart, Mock.inventory)
          .price
          .getOrElse(Double.MaxValue) <= Mock.nonOptimizedPrice shouldBe true
    }
  }
}
