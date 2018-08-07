package ca.cbotek.item.impl.entity

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import ca.cbotek.item.api.{Bundle, BundleItem, BundleRequestItem, Item}
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event._
import ca.cbotek.item.impl.model.ItemInventoryState
import ca.cbotek.item.impl.{ItemSerializerRegistry, ServiceErrors}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.util.Random

object ItemInventoryMock {
  def randomQuantity: Int = Random.nextInt(100)
  def randomPrice: Double = (Random.nextDouble() + 1.0) * 100
  def randomItemName: String = Random.nextString(10)
  def randomItem: Item = Item(UUID.randomUUID(), randomItemName, "description", randomPrice)

  final val newItem: Item = randomItem
  final val addItemCommand: AddItem = AddItem(newItem.id, newItem.name, newItem.description, newItem.price)
  final val itemAdded: ItemAdded = ItemAdded(newItem.id, newItem.name, newItem.description, newItem.price)

  final val deleteItem: DeleteItem = DeleteItem(newItem.id)
  final val itemDeleted: ItemDeleted = ItemDeleted(newItem.id)

  final val newBundleItem: BundleItem = BundleItem(randomQuantity, newItem)
  final val newBundle: Bundle = Bundle(Set(newBundleItem))
  final val addBundle: AddBundle =
    AddBundle(newBundle.id, newBundle.name, Iterable(BundleRequestItem(newBundleItem.quantity, newBundleItem.item.id)), newBundle.price)
  final val bundleAdded: BundleAdded = BundleAdded(newBundle.id, newBundle.name, newBundle.items, newBundle.price)

  final val deleteBundle: DeleteBundle = DeleteBundle(newBundle.id)
  final val bundleDeleted: BundleDeleted = BundleDeleted(newBundle.id)
}

class ItemInventoryEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("System", JsonSerializerRegistry.actorSystemSetupFor(ItemSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withEmptyState(block: PersistentEntityTestDriver[ItemInventoryCommand[_],
    ItemInventoryEvent,
    ItemInventoryState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ItemInventoryEntity {
      override val initialState: ItemInventoryState = ItemInventoryState.empty
    }, "inventory-empty")
    block(driver)
  }

  private def withItem(block: PersistentEntityTestDriver[ItemInventoryCommand[_],
    ItemInventoryEvent,
    ItemInventoryState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ItemInventoryEntity {
      override val initialState: ItemInventoryState = ItemInventoryState.empty.copy(items = Set(ItemInventoryMock.newItem))
    }, "inventory-one-item")
    block(driver)
  }

  private def withBundle(block: PersistentEntityTestDriver[ItemInventoryCommand[_],
    ItemInventoryEvent,
    ItemInventoryState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ItemInventoryEntity {
      override val initialState: ItemInventoryState = ItemInventoryState.empty.copy(bundles = Set(ItemInventoryMock.newBundle))
    }, "inventory-one-bundle")
    block(driver)
  }

  "ItemInventory entity" should {
    "Accept AddItem command with empty inventory" in withEmptyState { driver =>
      val outcome = driver.run(ItemInventoryMock.addItemCommand)
      outcome.replies should contain only Right(ItemInventoryMock.newItem)
      outcome.state shouldBe ItemInventoryState.empty.copy(items = Set(ItemInventoryMock.newItem))
      outcome.events should contain only ItemInventoryMock.itemAdded
    }

    "Reject AddItem command with item in inventory" in withItem { driver =>
      val outcome = driver.run(ItemInventoryMock.addItemCommand)
      outcome.replies should contain only Left(ServiceErrors.ItemConflict)
      outcome.state shouldBe ItemInventoryState.empty.copy(items = Set(ItemInventoryMock.newItem))
      outcome.events.isEmpty shouldBe true
    }

    "Accept DeleteItem command with item in inventory" in withItem { driver =>
      val outcome = driver.run(ItemInventoryMock.deleteItem)
      outcome.replies should contain only Right(ItemInventoryMock.newItem)
      outcome.state shouldBe ItemInventoryState.empty
      outcome.events should contain only ItemInventoryMock.itemDeleted
    }

    "Reject DeleteItem command with empty inventory" in withEmptyState { driver =>
      val outcome = driver.run(ItemInventoryMock.deleteItem)
      outcome.replies should contain only Left(ServiceErrors.ItemNotFound)
      outcome.state shouldBe ItemInventoryState.empty
      outcome.events.isEmpty shouldBe true
    }

    "Accept AddBundle command with bundle item(s) in inventory" in withItem { driver =>
      val outcome = driver.run(ItemInventoryMock.addBundle)
      outcome.replies should contain only Right(ItemInventoryMock.newBundle)
      outcome.state shouldBe ItemInventoryState.empty.copy(bundles = Set(ItemInventoryMock.newBundle), items = Set(ItemInventoryMock.newItem))
      outcome.events should contain only ItemInventoryMock.bundleAdded
    }

    "Reject AddBundle command with empty inventory" in withEmptyState { driver =>
      val outcome = driver.run(ItemInventoryMock.addBundle)
      outcome.replies should contain only Left(ServiceErrors.ItemsNotFoundInInventory)
      outcome.state shouldBe ItemInventoryState.empty
      outcome.events.isEmpty shouldBe true
    }

    "Reject AddBundle command with bundle in inventory" in withBundle { driver =>
      val outcome = driver.run(ItemInventoryMock.addBundle)
      outcome.replies should contain only Left(ServiceErrors.BundleConflict)
      outcome.state shouldBe ItemInventoryState.empty.copy(bundles = Set(ItemInventoryMock.newBundle))
      outcome.events.isEmpty shouldBe true
    }

    "Accept DeleteBundle command with item in inventory" in withBundle { driver =>
      val outcome = driver.run(ItemInventoryMock.deleteBundle)
      outcome.replies should contain only Right(ItemInventoryMock.newBundle)
      outcome.state shouldBe ItemInventoryState.empty
      outcome.events should contain only ItemInventoryMock.bundleDeleted
    }

    "Reject DeleteBundle command with empty inventory" in withEmptyState { driver =>
      val outcome = driver.run(ItemInventoryMock.deleteBundle)
      outcome.replies should contain only Left(ServiceErrors.BundleNotFound)
      outcome.state shouldBe ItemInventoryState.empty
      outcome.events.isEmpty shouldBe true
    }

    "Accept GetInventory command with item(s) in inventory" in withItem { driver =>
      driver.run(ItemInventoryMock.addBundle)
      val outcome = driver.run(GetInventory)
      outcome.replies should contain only ItemInventoryState(Set(ItemInventoryMock.newItem), Set(ItemInventoryMock.newBundle))
      outcome.state shouldBe ItemInventoryState.empty.copy(items = Set(ItemInventoryMock.newItem), bundles = Set(ItemInventoryMock.newBundle))
      outcome.events.isEmpty shouldBe true
    }
  }
}
