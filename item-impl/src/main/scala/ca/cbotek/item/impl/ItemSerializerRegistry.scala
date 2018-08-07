package ca.cbotek.item.impl

import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.event._
import ca.cbotek.item.impl.model.{CartState, ItemInventoryState}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object ItemSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // Command
    JsonSerializer[AddItem],
    JsonSerializer[DeleteItem],
    JsonSerializer[AddBundle],
    JsonSerializer[DeleteBundle],
    JsonSerializer[GetInventory.type],
    JsonSerializer[CreateCart],
    JsonSerializer[SetItemToCart],
    JsonSerializer[CheckoutCart],
    JsonSerializer[GetOneCart.type],
    // Event
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemDeleted],
    JsonSerializer[BundleAdded],
    JsonSerializer[BundleDeleted],
    JsonSerializer[CartCreated],
    JsonSerializer[CartItemsUpdated],
    JsonSerializer[CartCheckedout],
    // Model
    JsonSerializer[ItemInventoryState],
    JsonSerializer[CartState]
  )
}

