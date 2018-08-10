package ca.cbotek.item.impl

import ca.cbotek.item.api.ItemService
import ca.cbotek.item.impl.entity.{CartEntity, ItemInventoryEntity}
import ca.cbotek.item.impl.readside.{CartReadSideProcessor, CartRepository}
import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class ItemApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ItemApplication(context) {
      override def serviceLocator: ServiceLocator = ServiceLocator.NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ItemApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[ItemService])
}

abstract class ItemApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the services that this server provides
  override lazy val lagomServer: LagomServer = serverFor[ItemService](wire[ItemServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = ItemSerializerRegistry
  persistentEntityRegistry.register(wire[CartEntity])
  persistentEntityRegistry.register(wire[ItemInventoryEntity])
  readSide.register(wire[CartReadSideProcessor])

  lazy val itemService: ItemService = serviceClient.implement[ItemService]
  lazy val cartRepository: CartRepository = wire[CartRepository]
}
