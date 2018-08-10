package ca.cbotek.item

import com.typesafe.config.{Config, ConfigFactory}

package object impl {
  val config: Config = ConfigFactory.load()
  val itemInventoryEntityName: String = config.getString("service.item-inventory-entity.name")
}
