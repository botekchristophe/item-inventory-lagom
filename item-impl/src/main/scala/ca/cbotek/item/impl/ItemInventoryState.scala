package ca.cbotek.item.impl

import ca.cbotek.item.api.{Bundle, Item}
import play.api.libs.json.{Format, Json}

case class ItemInventoryState(items: Set[Item],
                              bundles: Set[Bundle])

object ItemInventoryState {
  implicit val format: Format[ItemInventoryState] = Json.format[ItemInventoryState]
}
