package ca.cbotek.cart.impl

import java.util.UUID

import akka.NotUsed
import ca.cbotek.cart.api._
import ca.cbotek.shared.{ErrorResponse, Marshaller}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

class CartServiceImpl(registry: PersistentEntityRegistry)
                     (implicit ec: ExecutionContext) extends CartService with Marshaller {

  private def refForCart(id: UUID) = registry.refFor[CartEntity](id.toString)

  override def getCarts: ServiceCall[NotUsed, Iterable[Cart]] =
    ServiceCall(_ => Future.successful(Iterable.empty[Cart]))

  override def createCart: ServiceCall[CartRequest, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "yet.")))
        .map(_.marshall)
    )

  override def addItemToCart(id: UUID): ServiceCall[CartItem, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "yet.")))
        .map(_.marshall)
    )

  override def checkout(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Checkout]] =
    ServerServiceCall((_, _) =>
      Future.successful(Left(ErrorResponse(501, "Not Implemented", "yet.")))
        .map(_.marshall)
    )
}

