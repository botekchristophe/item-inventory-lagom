package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, _}

//TODO add a discount percentage field which will allow to better optimize the cart
/**
  * A Bundle is a set of items which altogether have a different price than the items purchased one by one.
  *
  * @param id unique id of the bundle.
  * @param name unique name of the bundle.
  * @param items set of items defining this bundle.
  * @param price pri of purchase of the whole bundle.
  */
case class Bundle(id: UUID,
                  name: String,
                  items: Set[BundleItem],
                  price: Double)

object Bundle {
  implicit val format: Format[Bundle] = Json.format[Bundle]

  /**
    * Helper apply method which gives a simple way of creating a bundle based on only the set of items.
    * The price will be computed using 10% discount on each items multiplied by 99% to the power of the set size.
    * This last bit is added in order to emphasise on the fact that the more items are in a bundle the lower the price
    * is expected to be from a user perspective.
    *
    * @param items set of bundle items
    * @return a new bundle with
    */
  def apply(items: Set[BundleItem]): Bundle =
    Bundle(
      UUID.randomUUID(),
      name = items.map(_.item.name).mkString("", " - ", " Pack"),
      items,
      price = items.map(i => i.quantity * i.item.price * 0.9).sum * Math.pow(0.99, items.size))
}

/**
  * A bundleItem is an [[Item]] and a quantity representing the number of items being purchased while purchasing the
  * bundle.
  *
  * @param quantity number of items being part of the bundle.
  * @param item type of item being part of the bundle.
  */
case class BundleItem(quantity: Int,
                      item: Item)

object BundleItem {
  implicit val format: Format[BundleItem] = Json.format[BundleItem]
}

/**
  * BundleRequest is the representation of a bundle from a request to create a bundle persepective.
  * For instance, in a request to create a bundle, the unique id is not known yet and will be determined by the service
  * implementation.
  *
  * @param name unique name of the bundle
  * @param items bundle items request. Different model compared to [[BundleItem]]. See [[BundleRequestItem]] for more
  *              information.
  * @param price the purschase price of the bundle
  */
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

/**
  * A Bundle request item is part of [[BundleRequest]] and defines the items being part of this bundle with a quantity
  * of items and a unique identifier to that item.
  *
  * @param quantity item quantity in this bundle request
  * @param itemId item unique identifier in this bundle request
  */
case class BundleRequestItem(quantity: Int,
                             itemId: UUID)

object BundleRequestItem {
  implicit val format: Format[BundleRequestItem] = Json.format[BundleRequestItem]
}
