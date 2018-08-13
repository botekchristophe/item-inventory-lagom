package ca.cbotek.item.impl.command

import java.util.UUID

import ca.cbotek.item.api.bundle.{Bundle, BundleRequestItem}
import ca.cbotek.item.api.item.Item
import ca.cbotek.item.impl.ServiceErrors.ServiceError
import ca.cbotek.item.impl.model.ItemInventoryState
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

/**
  * ItemInventory command are case classes or case object that can be sent to a [[ca.cbotek.item.impl.entity.ItemInventoryEntity]]
  * as a message. A command response type is defined in order for the calling process to avoid type casting.
  *
  * @tparam R response type of the message.
  */
sealed trait ItemInventoryCommand[R] extends ReplyType[R]

/**
  * Add Item command
  *
  * This command represent a request to add an item to the Item Inventory.
  *
  * The Command is invalid if an item with the same name already exists.
  *
  * If the command is accepted, a [[ca.cbotek.item.impl.event.ItemAdded]] event will be persisted.
  *
  * @param id item id.
  * @param name item name.
  * @param description item long description.
  * @param price item unit price.
  */
case class AddItem(id: UUID,
                   name: String,
                   description: String,
                   price: Double) extends ItemInventoryCommand[Either[ServiceError, Item]]
object AddItem {
  implicit val format: Format[AddItem] = Json.format[AddItem]
}

/**
  * Delete Item command
  *
  * This command will send a request to delete an item from the Item Inventory.
  *
  * This command is invalid if no item with the specified identifier exists in the inventory or if a bundle currently uses
  * the selected item.
  *
  * If the command is accepted, a [[ca.cbotek.item.impl.event.ItemDeleted]] event will be persisted.
  *
  * @param id item unique identifier
  */
case class DeleteItem(id: UUID) extends ItemInventoryCommand[Either[ServiceError, Item]]
object DeleteItem {
  implicit val format: Format[DeleteItem] = Json.format[DeleteItem]
}

/**
  * Add Bundle command
  *
  * This command represent a request to add a new bundle to the Item Inventory.
  *
  * This command is invalid if a bundle with the same name already exists.
  *
  * If the command is accepted, a [[ca.cbotek.item.impl.event.BundleAdded]] event will be persisted.
  *
  * @param id bunde unique identifier.
  * @param name bundle name.
  * @param items bundle set of items along with the quantity per item.
  * @param average_discount bundle average discount percentage.
  */
case class AddBundle(id: UUID,
                     name: String,
                     items: Iterable[BundleRequestItem],
                     average_discount : Double) extends ItemInventoryCommand[Either[ServiceError, Bundle]]
object AddBundle {
  implicit val format: Format[AddBundle] = Json.format[AddBundle]
}


/**
  * Delete Bundle command
  *
  * This command will send a request to delete a bundle from the Item Inventory.
  *
  * This command is invalid if no bundle with the specified identifier exists in the inventory.
  *
  * If the command is accepted, a [[ca.cbotek.item.impl.event.BundleDeleted]] event will be persisted.
  *
  * @param id bundle unique identifier
  */
case class DeleteBundle(id: UUID) extends ItemInventoryCommand[Either[ServiceError, Bundle]]
object DeleteBundle {
  implicit val format: Format[DeleteBundle] = Json.format[DeleteBundle]
}

/**
  * Get Inventory command
  *
  * This command will sent a request to fetch the current state of the Item Inventory. The complete set of items and
  * bundles is returned.
  *
  * This command can be used with [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity.Actions.onReadOnlyCommand()]]
  *
  * This command is always valid.
  *
  * No events will be persisted in any cases
  */
case object GetInventory extends ItemInventoryCommand[ItemInventoryState] {
  implicit val format: Format[GetInventory.type] = singletonFormat(GetInventory)
}
