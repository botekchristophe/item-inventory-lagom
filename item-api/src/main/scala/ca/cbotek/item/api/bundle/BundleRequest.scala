package ca.cbotek.item.api.bundle

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

/**
  * BundleRequest is the representation of a bundle from a request to create a bundle persepective.
  * For instance, in a request to create a bundle, the unique id is not known yet and will be determined by the service
  * implementation.
  *
  * @param name unique name of the bundle
  * @param items bundle items request. Different model compared to [[BundleItem]]. See [[BundleRequestItem]] for more
  *              information.
  * @param average_discount the average discount for the bundle.
  *                         0.0 means 0% discount or in other words the bundle price correspond exactly to buying items one by one.
  *                         0.99 means 99% discount or in other words each items of the bundle will cost 99% less.
  */
case class BundleRequest(name: String,
                         items: Iterable[BundleRequestItem],
                         average_discount: Double)

object BundleRequest {
  implicit val reads: Reads[BundleRequest] = (
    (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "items").read[Iterable[BundleRequestItem]](minLength[Iterable[BundleRequestItem]](1)) and
      (JsPath \ "average_discount").read[Double](min[Double](0.00) keepAnd max[Double](0.99))
    )(BundleRequest.apply _)

  implicit val writes: Writes[BundleRequest] = Json.writes[BundleRequest]
  implicit val format: Format[BundleRequest] = Format(reads, writes)
}
