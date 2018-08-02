package ca.cbotek.item.api

import java.util.UUID

import play.api.libs.json._

case class Catalog(id: UUID,
                   items: Set[ItemRequest],
                   bundles: Set[BundleRequest])

object Catalog {
  implicit val format: Format[Catalog] = Json.format[Catalog]
}
