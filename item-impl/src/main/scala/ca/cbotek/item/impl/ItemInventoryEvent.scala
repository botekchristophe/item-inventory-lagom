package ca.cbotek.item.impl

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

trait ItemEvent extends AggregateEvent[ItemEvent] {
  override def aggregateTag: AggregateEventTagger[ItemEvent] = ItemEvent.Tag
}

object ItemEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[ItemEvent] = AggregateEventTag.sharded[ItemEvent](NumShards)
}

case class ItemAdded(id: UUID) extends ItemEvent
object ItemAdded {
  implicit val format: Format[ItemAdded] = Json.format[ItemAdded]
}

case class ItemDeleted(id: UUID) extends ItemEvent
object ItemDeleted {
  implicit val format: Format[ItemDeleted] = Json.format[ItemDeleted]
}

