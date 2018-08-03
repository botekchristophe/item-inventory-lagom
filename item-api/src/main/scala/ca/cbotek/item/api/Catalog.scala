package ca.cbotek.item.api

import play.api.libs.json._

case class Catalog(items: Set[Item],
                   bundles: Set[Bundle])

object Catalog {
  implicit val format: Format[Catalog] = Json.format[Catalog]
}
