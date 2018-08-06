package ca.cbotek.item.impl

import ca.cbotek.item.impl.command.{AddBundle, AddItem, DeleteBundle, DeleteItem}
import ca.cbotek.item.impl.event.{BundleAdded, BundleDeleted, ItemAdded, ItemDeleted}
import ca.cbotek.item.impl.model.ItemInventoryState
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object ItemSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // Command
    JsonSerializer[AddItem],
    JsonSerializer[DeleteItem],
    JsonSerializer[AddBundle],
    JsonSerializer[DeleteBundle],
    // Event
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemDeleted],
    JsonSerializer[BundleAdded],
    JsonSerializer[BundleDeleted],
    // Model
    JsonSerializer[ItemInventoryState],
  )
}

