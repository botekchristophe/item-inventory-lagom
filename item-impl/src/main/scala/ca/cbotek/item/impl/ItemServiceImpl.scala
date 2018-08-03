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
    ServerServiceCall((_, _) =>
      Future.successful(Catalog(Set.empty[Item], Set.empty[Bundle]))
      .map(_.marshall)
    )

  override def addItem: ServiceCall[ItemRequest, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "Soon available.")))
        .map(_.marshall)
    )

  override def addBundle: ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "Soon available.")))
        .map(_.marshall)
    )

  override def removeItem(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "Soon available.")))
        .map(_.marshall)
    )

  override def removeBundle(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "Soon available.")))
        .map(_.marshall)
    )
}
