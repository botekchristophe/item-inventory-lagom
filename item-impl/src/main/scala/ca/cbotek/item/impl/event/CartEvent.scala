package ca.cbotek.item.impl.event

import java.util.UUID

import ca.cbotek.item.api.cart.{CartBundle, CartItem}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}

/**
  * CartEvent group all events related to a [[ca.cbotek.item.impl.entity.CartEntity]].
  *
  * Cart event extends [[AggregateEvent]] in order to support a [[com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor]]
  *
  * The read side implementation will be in [[ca.cbotek.item.impl.readside.CartReadSideProcessor]]
  */
trait CartEvent extends AggregateEvent[CartEvent] {
  override def aggregateTag: AggregateEventTagger[CartEvent] = CartEvent.Tag
}

/**
  * Companion object of [[CartEvent]]
  *
  * Defines `Tag` which is an Aggregate Event tag, in this case it is shared in 4 shards.
  * Note that in production environment, the number of shards can be hard to update once set.
  */
object CartEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[CartEvent] = AggregateEventTag.sharded[CartEvent](NumShards)
}

/**
  * Event CartCreated
  *
  * Can be persisted following a command [[ca.cbotek.item.impl.command.CreateCart]].
  *
  * @param id cart unique identifier
  * @param user owner of the cart
  * @param items items belonging to this cart
  * @param bundles bundles belonging to this cart
  */
case class CartCreated(id: UUID,
                       user: String,
                       items: Set[CartItem],
                       bundles: Set[CartBundle]) extends CartEvent
object CartCreated {
  implicit val format: Format[CartCreated] = Json.format[CartCreated]
}

/**
  * Event CartUpdated
  *
  * Can be persisted following a command [[ca.cbotek.item.impl.command.UpdateCart]].
  *
  * @param id cart unique identifier
  * @param items cart new set of items
  * @param bundles cart new set of bundles
  */
case class CartUpdated(id: UUID,
                       items: Set[CartItem],
                       bundles: Set[CartBundle]) extends CartEvent
object CartUpdated {
  implicit val format: Format[CartUpdated] = Json.format[CartUpdated]
}

/**
  * Event CartCheckedOut
  *
  * Can be persisted following a command [[ca.cbotek.item.impl.command.CheckoutCart]].
  *
  * @param id cart unique identifier
  */
case class CartCheckedOut(id: UUID) extends CartEvent
object CartCheckedOut {
  implicit val format: Format[CartCheckedOut] = Json.format[CartCheckedOut]
}
