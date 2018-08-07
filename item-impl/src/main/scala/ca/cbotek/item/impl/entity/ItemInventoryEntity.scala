package ca.cbotek.item.impl.entity

import java.util.UUID

import ca.cbotek.item.api.{Bundle, BundleItem, BundleRequestItem, Item}
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
        case (ItemAdded(id, name, description, price), inventory) =>
          inventory.copy(items = inventory.items + Item(id, name, description, price))

        case (ItemDeleted(id), inventory) =>
          inventory.copy(items = inventory.items.filterNot(_.id == id))

        case (BundleAdded(id, name, items, price), inventory) =>
          val bundleItems = items.map(it => BundleItem(it.quantity, inventory.items.find(_.id == it.item.id).get))
          inventory.copy(bundles = inventory.bundles + Bundle(id, name, bundleItems, price))

        case (BundleDeleted(id), inventory) =>
          inventory.copy(bundles = inventory.bundles.filterNot(_.id == id))

        case (_, state) => state
      }
  }

  private def addItem: OnCommandHandler[Either[ServiceError, Item]] = {
    case (AddItem(id, name, description, price), ctx, inventory) =>
      inventory.items.find(_.name == name) match {
        case None =>
          ctx.thenPersist(ItemAdded(id, name, description, price))(_ => ctx.reply(Right(Item(id, name, description, price))))
        case Some(_) =>
          ctx.reply(Left(ItemConflict))
          ctx.done
      }
  }

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

  private def addBundle: OnCommandHandler[Either[ServiceError, Bundle]] = {
    case (AddBundle(id, name, items, price), ctx, inventory) =>
      inventory.bundles.find(_.name == name) match {
        case None =>
          if (inventoryContainsItems(inventory, items)) {
            val bundleItems = items.map(item => BundleItem(item.quantity, inventory.items.find(_.id == item.itemId).get)).toSet
            ctx.thenPersist(BundleAdded(id, name, bundleItems, price))(_ => ctx.reply(Right(Bundle(id, name, bundleItems, price))))
          } else {
            ctx.reply(Left(ItemsNotFoundInInventory))
            ctx.done
          }
        case Some(_) =>
          ctx.reply(Left(BundleConflict))
          ctx.done
      }
  }

  private def inventoryContainsItems(inventory: ItemInventoryState, items: Iterable[BundleRequestItem]): Boolean =
    inventory.items.map(_.id).toList.intersect(items.map(_.itemId).toList).size == items.size

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

  private def getInventory: ReadOnlyHandler[ItemInventoryState] = {
    case (GetInventory, ctx, inventory) => ctx.reply(inventory)
  }
}
