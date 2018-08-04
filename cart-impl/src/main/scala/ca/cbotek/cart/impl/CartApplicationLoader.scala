package ca.cbotek.cart.impl

import ca.cbotek.cart.api.CartService
import ca.cbotek.item.api.ItemService
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.ConfigurationServiceLocatorComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class CartApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CartApplication(context) with ConfigurationServiceLocatorComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CartApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[CartService])
}

abstract class CartApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  override lazy val lagomServer: LagomServer = serverFor[CartService](wire[CartServiceImpl])
  persistentEntityRegistry.register(wire[CartEntity])
  readSide.register(wire[CartReadSideProcessor])
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = CartSerializerRegistry

  lazy val itemService: ItemService = serviceClient.implement[ItemService]
  lazy val cartRepository: CartRepository = wire[CartRepository]
}

