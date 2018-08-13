package ca.cbotek.item.impl.entity

import java.util.UUID

import ca.cbotek.item.api.bundle.{Bundle, BundleItem, BundleRequestItem}
import ca.cbotek.item.api.item.Item
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event._
import ca.cbotek.item.impl.model.ItemInventoryState
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.slf4j.LoggerFactory

class ItemInventoryEntity extends PersistentEntity {

  override type Command = ItemInventoryCommand[_]
  override type Event = ItemInventoryEvent
  override type State = ItemInventoryState

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  override def initialState: ItemInventoryState = ItemInventoryState.empty

  var currentState: ItemInventoryState = initialState

  private val log = LoggerFactory.getLogger(classOf[ItemInventoryEntity])

  override def behavior: Behavior = {
    Actions()
      .onCommand[AddItem, Either[ServiceError, Item]] { addItem }
      .onCommand[DeleteItem, Either[ServiceError, Item]] { deleteItem }
      .onCommand[AddBundle, Either[ServiceError, Bundle]] { addBundle }
      .onCommand[DeleteBundle, Either[ServiceError, Bundle]] { deleteBundle }
      .onReadOnlyCommand[GetInventory.type, ItemInventoryState] { getInventory }
      .onEvent {
        case (ItemAdded(i), inventory) =>
          inventory.copy(items = inventory.items + i)

        case (ItemDeleted(id), inventory) =>
          inventory.copy(items = inventory.items.filterNot(_.id == id))

        case (BundleAdded(b), inventory) =>
          inventory.copy(bundles = inventory.bundles + b)

        case (BundleDeleted(id), inventory) =>
          inventory.copy(bundles = inventory.bundles.filterNot(_.id == id))

        case (_, state) => state
      }
  }

  /**
    * method validating an incoming [[AddItem]] message.
    *
    * Checks if an Item already exists with the same name
    *
    * @return if the item is valid, returns the item info and persist [[ItemAdded]]
    *         if the item name is in conflict, returns [[ItemConflict]] and persist nothing
    */
  private def addItem: OnCommandHandler[Either[ServiceError, Item]] = {
    case (AddItem(id, name, description, price), ctx, inventory) =>
      inventory.items.find(_.name == name) match {
        case None =>
          val newItem = Item(id, name, description, price)
          ctx.thenPersist(ItemAdded(newItem))(_ => ctx.reply(Right(newItem)))
        case Some(_) =>
          ctx.reply(Left(ItemConflict))
          ctx.done
      }
  }

  /**
    * method validating an incoming [[DeleteItem]] message.
    *
    * Checks if the targeted item exists.
    *
    * @return if the item exists, returns the item info and persist [[ItemDeleted]]
    *         if the item is not found, returns [[ItemNotFound]] and persist nothing
    */
  private def deleteItem: OnCommandHandler[Either[ServiceError, Item]] = {
    case (DeleteItem(id), ctx, inventory) =>
      inventory.items.find(_.id == id) match {
        case None =>
          ctx.reply(Left(ItemNotFound))
          ctx.done
        case Some(item) =>
          val bundleItemIds: Set[UUID] = inventory.bundles.flatMap(_.items.map(_.item.id))
          if (bundleItemIds.contains(id)) {
            ctx.reply(Left(ItemCannotBeRemoved))
            ctx.done
          } else {
            ctx.thenPersist(ItemDeleted(id))(_ => ctx.reply(Right(item)))
          }
      }
  }

  /**
    * method validating an incoming [[AddBundle]] message.
    *
    * Checks if a bundle already exists with the same name
    * and
    * Checks if all the items present in the bundle exists in the inventory.
    *
    * @return if the bundle is valid, returns the bundle info and persist [[BundleAdded]]
    *         if the bundle name is in conflict, returns [[BundleConflict]] and persist nothing
    *         if the bundle items are not all found, returns [[ItemsNotFoundInInventory]] and persist nothing
    */
  private def addBundle: OnCommandHandler[Either[ServiceError, Bundle]] = {
    case (AddBundle(id, name, items, average_discount), ctx, inventory) =>
      inventory.bundles.find(_.name == name) match {
        case None =>
          if (inventoryContainsItems(inventory, items)) {
            val bundleItems = items.map(item => BundleItem(item.quantity, inventory.items.find(_.id == item.itemId).get)).toSet
            val newBundle = Bundle(id, name, bundleItems, average_discount)
            ctx.thenPersist(BundleAdded(newBundle))(_ => ctx.reply(Right(newBundle)))
          } else {
            ctx.reply(Left(ItemsNotFoundInInventory))
            ctx.done
          }
        case Some(_) =>
          ctx.reply(Left(BundleConflict))
          ctx.done
      }
  }

  /**
    * Helper method checking that all items passed in parameters are present in the inventory
    *
    * @param inventory item inventory
    * @param items list of items to check
    * @return true if all items passed are present in the inventoty
    *         false if one or more items does not exists
    */
  private def inventoryContainsItems(inventory: ItemInventoryState, items: Iterable[BundleRequestItem]): Boolean =
    inventory.items.map(_.id).toList.intersect(items.map(_.itemId).toList).size == items.size

  /**
    * method validating an incoming [[DeleteBundle]] message.
    *
    * Checks if the targeted bundle exists.
    *
    * @return if the bundle exists, returns the bundle info and persist [[BundleDeleted]]
    *         if the bundle is not found, returns [[BundleNotFound]] and persist nothing
    */
  private def deleteBundle: OnCommandHandler[Either[ServiceError, Bundle]] = {
    case (DeleteBundle(id), ctx, inventory) =>
      inventory.bundles.find(_.id == id) match {
        case None =>
          ctx.reply(Left(BundleNotFound))
          ctx.done
        case Some(item) =>
          ctx.thenPersist(BundleDeleted(id))(_ => ctx.reply(Right(item)))
      }
  }

  /**
    * method handling [[GetInventory]] command
    *
    * @return the complete inventory without filter.
    */
  private def getInventory: ReadOnlyHandler[ItemInventoryState] = {
    case (GetInventory, ctx, inventory) => ctx.reply(inventory)
  }
}
