package ca.cbotek.item.impl.event

import java.util.UUID

import ca.cbotek.item.api.BundleItem
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

trait ItemInventoryEvent extends AggregateEvent[ItemInventoryEvent] {
  override def aggregateTag: AggregateEventTagger[ItemInventoryEvent] = ItemInventoryEvent.Tag
}

object ItemInventoryEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[ItemInventoryEvent] = AggregateEventTag.sharded[ItemInventoryEvent](NumShards)
}

case class ItemAdded(id: UUID,
                     name: String,
                     description: String,
                     price: Double) extends ItemInventoryEvent
object ItemAdded {
  implicit val format: Format[ItemAdded] = Json.format[ItemAdded]
}

case class ItemDeleted(id: UUID) extends ItemInventoryEvent
object ItemDeleted {
  implicit val format: Format[ItemDeleted] = Json.format[ItemDeleted]
}

case class BundleAdded(id: UUID,
                       name: String,
                       items: Set[BundleItem],
                       price: Double) extends ItemInventoryEvent
object BundleAdded {
  implicit val format: Format[BundleAdded] = Json.format[BundleAdded]
}

case class BundleDeleted(id: UUID) extends ItemInventoryEvent
object BundleDeleted {
  implicit val format: Format[BundleDeleted] = Json.format[BundleDeleted]
}
