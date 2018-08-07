package ca.cbotek.item.impl.entity

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import ca.cbotek.item.api.{Cart, CartItem}
import ca.cbotek.item.impl.ItemSerializerRegistry
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command.{CartCommand, CheckoutCart, CreateCart, SetItemToCart}
import ca.cbotek.item.impl.event.{CartCheckedout, CartCreated, CartEvent, CartItemsUpdated}
import ca.cbotek.item.impl.model.{CartState, CartStatuses}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.util.Random

object CartEntityMock {

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

class CartEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("System", JsonSerializerRegistry.actorSystemSetupFor(ItemSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withEmptyState(block: PersistentEntityTestDriver[CartCommand[_], CartEvent, Option[CartState]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CartEntity {
      override val initialState: Option[CartState] = None
    }, "cart-empty")
    block(driver)
  }

  private def withCartInUse(block: PersistentEntityTestDriver[CartCommand[_], CartEvent, Option[CartState]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CartEntity {
      override val initialState: Option[CartState] = Some(CartEntityMock.cartInUseState)
    }, "cart-in-use")
    block(driver)
  }

  private def withCartCheckedOut(block: PersistentEntityTestDriver[CartCommand[_], CartEvent, Option[CartState]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CartEntity {
      override val initialState: Option[CartState] = Some(CartEntityMock.cartCheckedoutState)
    }, "cart-checked-out")
    block(driver)
  }

  "Cart entity" should {
    "Accept CreateCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(CartEntityMock.createCartCommand)
      outcome.replies should contain only Right(CartEntityMock.createCartResponse)
      outcome.state shouldBe Some(CartEntityMock.cartInUseState)
      outcome.events should contain only CartEntityMock.cartCreated
    }

    "Reject CreateCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(CartEntityMock.createCartCommand)
      outcome.replies should contain only Left(CartConflict)
      outcome.state shouldBe Some(CartEntityMock.cartInUseState)
      outcome.events.isEmpty shouldBe true
    }

    "Reject CreateCart command with CHECKED_OUT state" in withCartInUse { driver =>
      val outcome = driver.run(CartEntityMock.createCartCommand)
      outcome.replies should contain only Left(CartConflict)
      outcome.state shouldBe Some(CartEntityMock.cartInUseState)
      outcome.events.isEmpty shouldBe true
    }

    "Accept SetItemToCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(CartEntityMock.setItemToCartCommand)
      outcome.replies should contain only Right(CartEntityMock.setItemToCartResponse)
      outcome.state shouldBe Some(CartEntityMock.cartInUseStateWithUpdate)
      outcome.events should contain only CartEntityMock.cartItemsUpdated
    }

    "Reject SetItemToCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(CartEntityMock.setItemToCartCommand)
      outcome.replies should contain only Left(CartNotFound)
      outcome.state shouldBe None
      outcome.events.isEmpty shouldBe true
    }

    "Reject SetItemToCart command with CHECKED_OUT state" in withCartCheckedOut { driver =>
      val outcome = driver.run(CartEntityMock.setItemToCartCommand)
      outcome.replies should contain only Left(CartCheckedOut)
      outcome.state shouldBe Some(CartEntityMock.cartCheckedoutState)
      outcome.events.isEmpty shouldBe true
    }

    "Accept CheckoutCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(CartEntityMock.checkoutCartCommand)
      outcome.replies should contain only Right(CartEntityMock.checkoutCartResponse)
      outcome.state shouldBe Some(CartEntityMock.cartCheckedoutState)
      outcome.events should contain only CartEntityMock.cartCheckedout
    }

    "Reject CheckoutCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(CartEntityMock.setItemToCartCommand)
      outcome.replies should contain only Left(CartNotFound)
      outcome.state shouldBe None
      outcome.events.isEmpty shouldBe true
    }

    "Reject CheckoutCart command with CHECKED_OUT state" in withCartCheckedOut { driver =>
      val outcome = driver.run(CartEntityMock.setItemToCartCommand)
      outcome.replies should contain only Left(CartCheckedOut)
      outcome.state shouldBe Some(CartEntityMock.cartCheckedoutState)
      outcome.events.isEmpty shouldBe true
    }
  }
}
