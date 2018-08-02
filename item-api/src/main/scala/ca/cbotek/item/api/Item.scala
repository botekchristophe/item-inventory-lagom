package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

case class Item(id: UUID,
                name: String,
                description: String,
                price: Double)

object Item {
  implicit val format: Format[Item] = Json.format[Item]
}

case class ItemRequest(name: String,
                       description: String,
                       price: Double)

object ItemRequest {
  implicit val reads: Reads[ItemRequest] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").read[String](minLength[String](2)) and
      (JsPath \ "price").read[Double](min[Double](0.01))
    )(ItemRequest.apply _)

  implicit val writes: Writes[ItemRequest] = Json.writes[ItemRequest]
  implicit val format: Format[ItemRequest] = Format(reads, writes)
}
