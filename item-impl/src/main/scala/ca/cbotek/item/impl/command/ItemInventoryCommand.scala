package ca.cbotek.item.impl.command

import java.util.UUID

import ca.cbotek.item.api.{Bundle, BundleRequestItem, Item}
import ca.cbotek.item.impl.model.ItemInventoryState
import ca.cbotek.shared.ErrorResponse
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait ItemInventoryCommand[R] extends ReplyType[R]

case class AddItem(id: UUID,
                   name: String,
                   description: String,
                   price: Double) extends ItemInventoryCommand[Either[ErrorResponse, Item]]
object AddItem {
  implicit val format: Format[AddItem] = Json.format[AddItem]
}

case class DeleteItem(id: UUID) extends ItemInventoryCommand[Either[ErrorResponse, Item]]
object DeleteItem {
  implicit val format: Format[DeleteItem] = Json.format[DeleteItem]
}

case class AddBundle(id: UUID,
                     name: String,
                     items: Iterable[BundleRequestItem],
                     price: Double) extends ItemInventoryCommand[Either[ErrorResponse, Bundle]]
object AddBundle {
  implicit val format: Format[AddBundle] = Json.format[AddBundle]
}

case class DeleteBundle(id: UUID) extends ItemInventoryCommand[Either[ErrorResponse, Bundle]]
object DeleteBundle {
  implicit val format: Format[DeleteBundle] = Json.format[DeleteBundle]
}

case object GetInventory extends ItemInventoryCommand[ItemInventoryState] {
  implicit val format: Format[GetInventory.type] = singletonFormat(GetInventory)
}
