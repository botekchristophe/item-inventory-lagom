package ca.cbotek.item.impl.model

import ca.cbotek.item.api.{Bundle, Item}
import play.api.libs.json.{Format, Json}

/**
  * An Item Inventory State
  *
  * This class holds the state of a [[ca.cbotek.item.impl.entity.ItemInventoryEntity]] in the form of two sets.
  * On set contains all the bundles and the other set contain all the items the of the inventory.
  *
  * @param items set of [[Item]]. Note that each [[Item]] has to be unique
  * @param bundles set of [[Bundle]]. Note that each [[Bundle]] has to be unique
  */
case class ItemInventoryState(items: Set[Item],
                              bundles: Set[Bundle])

object ItemInventoryState {

  implicit val format: Format[ItemInventoryState] = Json.format[ItemInventoryState]

  /**
    * empty method defining a state where the inventory is empty.
    *
    * @return an empty inventory with both items and bundles sets empty.
    */
  def empty: ItemInventoryState =
    ItemInventoryState(
      items = Set.empty[Item],
      bundles = Set.empty[Bundle]
    )
}
