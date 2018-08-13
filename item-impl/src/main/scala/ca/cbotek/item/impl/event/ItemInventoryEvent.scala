package ca.cbotek.item.impl.event

import java.util.UUID

import ca.cbotek.item.api.bundle.{Bundle, BundleItem}
import ca.cbotek.item.api.item.Item
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

/**
  * ItemInventoryEvent group all events related to a [[ca.cbotek.item.impl.entity.ItemInventoryEntity]].
  *
  * Cart event extends [[AggregateEvent]] in order to support a [[com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor]]
  *
  * No implementation of a ReadSide processor is available for [[ItemInventoryEvent]]
  */
trait ItemInventoryEvent extends AggregateEvent[ItemInventoryEvent] {
  override def aggregateTag: AggregateEventTagger[ItemInventoryEvent] = ItemInventoryEvent.Tag
}

object ItemInventoryEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[ItemInventoryEvent] = AggregateEventTag.sharded[ItemInventoryEvent](NumShards)
}

/**
  * ItemAdded event
  *
  * Can be persisted following a [[ca.cbotek.item.impl.command.AddItem]] command.
  *
  * @param item item to be persisted
  */
case class ItemAdded(item: Item) extends ItemInventoryEvent
object ItemAdded {
  implicit val format: Format[ItemAdded] = Json.format[ItemAdded]
}

/**
  * ItemDeleted event
  *
  * Can be persisted following a [[ca.cbotek.item.impl.command.DeleteItem]] command.
  *
  * @param id item unique identifier
  */
case class ItemDeleted(id: UUID) extends ItemInventoryEvent
object ItemDeleted {
  implicit val format: Format[ItemDeleted] = Json.format[ItemDeleted]
}

/**
  * BundleAdded event
  *
  * Can be persisted following a [[ca.cbotek.item.impl.command.AddBundle]] command.
  *
  * @param bundle bundle to be persisted
  */
case class BundleAdded(bundle: Bundle) extends ItemInventoryEvent
object BundleAdded {
  implicit val format: Format[BundleAdded] = Json.format[BundleAdded]
}

/**
  * BundleDeleted event
  *
  * Can be persisted following a [[ca.cbotek.item.impl.command.DeleteBundle]] command.
  *
  * @param id bundle unique identifier
  */
case class BundleDeleted(id: UUID) extends ItemInventoryEvent
object BundleDeleted {
  implicit val format: Format[BundleDeleted] = Json.format[BundleDeleted]
}
