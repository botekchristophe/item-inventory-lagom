package ca.cbotek.item.api.bundle

import java.util.UUID

import play.api.libs.json._

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
