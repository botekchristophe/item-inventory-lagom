package ca.cbotek.item.impl

import java.util.UUID

import akka.Done
import ca.cbotek.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}

sealed trait ItemCommand[R] extends ReplyType[R]

case class AddItem(id: UUID) extends ItemCommand[Either[ErrorResponse, Done]]
object AddItem {
  implicit val format: Format[AddItem] = Json.format[AddItem]
}

case class DeleteItem(id: UUID) extends ItemCommand[Either[ErrorResponse, Done]]
object DeleteItem {
  implicit val format: Format[DeleteItem] = Json.format[DeleteItem]
}
