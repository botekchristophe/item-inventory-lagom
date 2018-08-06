package ca.cbotek.item.impl.event

import java.util.UUID

import ca.cbotek.item.api.CartItem
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

trait CartEvent extends AggregateEvent[CartEvent] {
  override def aggregateTag: AggregateEventTagger[CartEvent] = CartEvent.Tag
}

object CartEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[CartEvent] = AggregateEventTag.sharded[CartEvent](NumShards)
}

case class CartCreated(id: UUID,
                       user: String,
                       items: Set[CartItem]) extends CartEvent
object CartCreated {
  implicit val format: Format[CartCreated] = Json.format[CartCreated]
}

case class CartItemsUpdated(id: UUID,
                            items: Set[CartItem]) extends CartEvent
object CartItemsUpdated {
  implicit val format: Format[CartItemsUpdated] = Json.format[CartItemsUpdated]
}

case class CartCheckedout(id: UUID, price: Double) extends CartEvent
object CartCheckedout {
  implicit val format: Format[CartCheckedout] = Json.format[CartCheckedout]
}
