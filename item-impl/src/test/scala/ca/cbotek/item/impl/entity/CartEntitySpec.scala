package ca.cbotek.item.impl.entity

import akka.actor.ActorSystem
import akka.testkit.TestKit
import ca.cbotek.item.impl.ItemSerializerRegistry
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command.CartCommand
import ca.cbotek.item.impl.event.CartEvent
import ca.cbotek.item.impl.model.CartState
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

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
      override val initialState: Option[CartState] = Some(Mock.cartInUseState)
    }, "cart-in-use")
    block(driver)
  }

  private def withCartCheckedOut(block: PersistentEntityTestDriver[CartCommand[_], CartEvent, Option[CartState]] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new CartEntity {
      override val initialState: Option[CartState] = Some(Mock.cartCheckedoutState)
    }, "cart-checked-out")
    block(driver)
  }

  "Cart entity" should {
    "Accept CreateCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(Mock.createCartCommand)
      outcome.replies should contain only Right(Mock.createCartResponse)
      outcome.state shouldBe Some(Mock.cartInUseState)
      outcome.events should contain only Mock.cartCreated
    }

    "Reject CreateCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(Mock.createCartCommand)
      outcome.replies should contain only Left(CartConflict)
      outcome.state shouldBe Some(Mock.cartInUseState)
      outcome.events.isEmpty shouldBe true
    }

    "Reject CreateCart command with CHECKED_OUT state" in withCartInUse { driver =>
      val outcome = driver.run(Mock.createCartCommand)
      outcome.replies should contain only Left(CartConflict)
      outcome.state shouldBe Some(Mock.cartInUseState)
      outcome.events.isEmpty shouldBe true
    }

    "Accept SetItemToCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(Mock.setItemToCartCommand)
      outcome.replies should contain only Right(Mock.setItemToCartResponse)
      outcome.state shouldBe Some(Mock.cartInUseStateWithUpdate)
      outcome.events should contain only Mock.cartItemsUpdated
    }

    "Reject SetItemToCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(Mock.setItemToCartCommand)
      outcome.replies should contain only Left(CartNotFound)
      outcome.state shouldBe None
      outcome.events.isEmpty shouldBe true
    }

    "Reject SetItemToCart command with CHECKED_OUT state" in withCartCheckedOut { driver =>
      val outcome = driver.run(Mock.setItemToCartCommand)
      outcome.replies should contain only Left(CartCheckedOut)
      outcome.state shouldBe Some(Mock.cartCheckedoutState)
      outcome.events.isEmpty shouldBe true
    }

    "Accept CheckoutCart command with IN_USE state" in withCartInUse { driver =>
      val outcome = driver.run(Mock.checkoutCartCommand)
      outcome.replies should contain only Right(Mock.checkoutCartResponse)
      outcome.state shouldBe Some(Mock.cartCheckedoutState)
      outcome.events should contain only Mock.cartCheckedout
    }

    "Reject CheckoutCart command with empty state" in withEmptyState { driver =>
      val outcome = driver.run(Mock.setItemToCartCommand)
      outcome.replies should contain only Left(CartNotFound)
      outcome.state shouldBe None
      outcome.events.isEmpty shouldBe true
    }

    "Reject CheckoutCart command with CHECKED_OUT state" in withCartCheckedOut { driver =>
      val outcome = driver.run(Mock.setItemToCartCommand)
      outcome.replies should contain only Left(CartCheckedOut)
      outcome.state shouldBe Some(Mock.cartCheckedoutState)
      outcome.events.isEmpty shouldBe true
    }
  }
}
