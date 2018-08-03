package ca.cbotek.item.impl

import java.util.UUID

import akka.NotUsed
import ca.cbotek.item.api._
import ca.cbotek.shared.{ErrorResponse, Marshaller}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

class ItemServiceImpl(registry: PersistentEntityRegistry)
                     (implicit ec: ExecutionContext) extends ItemService with Marshaller {

  private def refForInventory = registry.refFor[ItemInventoryEntity]("item-inventory")

  override def getCatalog: ServiceCall[NotUsed, Catalog] =
    ServiceCall(_ =>
      refForInventory
        .ask(GetInventory)
        .map(inventory => Catalog(inventory.items, inventory.bundles))
    )

  override def addItem: ServiceCall[ItemRequest, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddItem(UUID.randomUUID(), request.name, request.description, request.price))
        .map(_.marshall)
    )

  override def addBundle: ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddBundle(UUID.randomUUID(), request.name, request.items, request.price))
        .map(_.marshall)
    )

  override def removeItem(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, _) =>
      refForInventory.ask(DeleteItem(id))
        .map(_.marshall)
    )

  override def removeBundle(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, _) =>
      refForInventory
        .ask(DeleteBundle(id))
        .map(_.marshall)
    )
}
