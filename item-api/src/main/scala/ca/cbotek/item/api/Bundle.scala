package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

case class Bundle(id: UUID,
                  name: String,
                  items: Set[BundleItem],
                  price: Double)

object Bundle {
  implicit val format: Format[Bundle] = Json.format[Bundle]
}

case class BundleItem(quantity: Int,
                      item: Item)

object BundleItem {
  implicit val format: Format[BundleItem] = Json.format[BundleItem]
}

case class BundleRequest(name: String,
                         items: Iterable[BundleRequestItem],
                         price: Double)

object BundleRequest {
  implicit val reads: Reads[BundleRequest] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "items").read[Iterable[BundleRequestItem]](minLength[Iterable[BundleRequestItem]](1)) and
      (JsPath \ "price").read[Double](min[Double](0.01))
    )(BundleRequest.apply _)

  implicit val writes: Writes[BundleRequest] = Json.writes[BundleRequest]
  implicit val format: Format[BundleRequest] = Format(reads, writes)
}

case class BundleRequestItem(quantity: Int,
                             itemId: UUID)

object BundleRequestItem {
  implicit val format: Format[BundleRequestItem] = Json.format[BundleRequestItem]
}
