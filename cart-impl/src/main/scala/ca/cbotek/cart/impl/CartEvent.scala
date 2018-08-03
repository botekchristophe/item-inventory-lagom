package ca.cbotek.cart.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}

trait CartEvent extends AggregateEvent[CartEvent] {
  override def aggregateTag: AggregateEventTagger[CartEvent] = CartEvent.Tag
}

object CartEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[CartEvent] = AggregateEventTag.sharded[CartEvent](NumShards)
}
