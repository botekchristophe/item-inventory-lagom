package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

case class Bundle(id: UUID,
                  name: String,
                  items: Set[UUID],
                  price: Double)

object Bundle {
  implicit val format: Format[Bundle] = Json.format[Bundle]
}

case class BundleRequest(name: String,
                         items: Iterable[UUID],
                         price: Double)

object BundleRequest {
  implicit val reads: Reads[BundleRequest] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "items").read[Iterable[UUID]](minLength[Iterable[UUID]](1)) and
      (JsPath \ "price").read[Double](min[Double](0.01))
    )(BundleRequest.apply _)

  implicit val writes: Writes[BundleRequest] = Json.writes[BundleRequest]
  implicit val format: Format[BundleRequest] = Format(reads, writes)
}
