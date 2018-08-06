package ca.cbotek.item.impl.model

import ca.cbotek.item.api.{Bundle, Item}
import play.api.libs.json.{Format, Json}

case class ItemInventoryState(items: Set[Item],
                              bundles: Set[Bundle])

object ItemInventoryState {

  implicit val format: Format[ItemInventoryState] = Json.format[ItemInventoryState]

  def empty: ItemInventoryState =
    ItemInventoryState(
      items = Set.empty[Item],
      bundles = Set.empty[Bundle]
    )
}
