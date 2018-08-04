package ca.cbotek.cart.impl

import java.util.UUID

import akka.NotUsed
import ca.cbotek.cart.api._
import ca.cbotek.item.api.{Catalog, ItemService}
import ca.cbotek.shared.{ErrorResponse, Marshaller}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

class CartServiceImpl(registry: PersistentEntityRegistry,
                      itemService: ItemService,
                      cartRepository: CartRepository)
                     (implicit ec: ExecutionContext) extends CartService with Marshaller {

  private def refForCart(id: UUID) = registry.refFor[CartEntity](id.toString)

  override def getCarts: ServiceCall[NotUsed, Iterable[Cart]] =
    ServiceCall(_ => cartRepository.getCarts)

  override def createCart: ServiceCall[CartRequest, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, request) =>
      checkItemIdsExist(request.items.map(_.itemId)).flatMap(_.fold(
        e => Future.successful(Left(e)),
        _ => {
          val cartId: UUID = UUID.randomUUID()
          refForCart(cartId).ask(CreateCart(cartId, request.user, request.items))}))
        .map(_.marshall)
    )

  override def setQuantityForCartItem(cartId: UUID, itemId: UUID, quantity: Int): ServiceCall[NotUsed, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, _) =>
      Right(quantity)
        .filterOrElse(qtt => qtt >= 0, ErrorResponse(400, "Bad request", "Quantity cannot be nagative."))
        .fold(
          e => Future.successful(Left(e)),
          _ => checkItemIdsExist(Set(itemId)).flatMap(_.fold(
            e => Future.successful(Left(e)),
            _ => refForCart(cartId).ask(SetItemToCart(cartId, itemId, quantity)))))
        .map(_.marshall)
    )

  override def checkout(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Checkout]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "yet.")))
        .map(_.marshall)
    )

  private def checkItemIdsExist(itemIds: Set[UUID]): Future[Either[ErrorResponse, Catalog]] =
    itemService
      .getCatalog
      .invoke()
      .map(catalog =>
        if (catalog.items.map(_.id).intersect(itemIds).size == itemIds.size) {
          Right(catalog)
        } else {
          Left(ErrorResponse(400, "Bad Request", "Item(s) not in catalog."))
        })
}

