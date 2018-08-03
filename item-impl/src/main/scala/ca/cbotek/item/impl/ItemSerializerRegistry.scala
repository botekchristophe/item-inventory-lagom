package ca.cbotek.item.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object ItemSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // Command
    JsonSerializer[AddItem],
    JsonSerializer[DeleteItem],
    // Event
    JsonSerializer[ItemAdded],
    JsonSerializer[ItemDeleted],
    // Model
    JsonSerializer[ItemInventoryState],
  )
}

