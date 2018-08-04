package ca.cbotek.cart.api

import java.util.UUID

import akka.NotUsed
import ca.cbotek.shared.ErrorResponse
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.{Environment, Mode}

trait CartService extends Service {

  def getCarts:                                                           ServiceCall[NotUsed, Iterable[Cart]]
  def createCart:                                                         ServiceCall[CartRequest, Either[ErrorResponse, Cart]]
  def setQuantityForCartItem(cartId: UUID, itemId: UUID, quantity: Int):  ServiceCall[NotUsed, Either[ErrorResponse, Cart]]
  def checkout(id: UUID):                                                 ServiceCall[NotUsed, Either[ErrorResponse, Checkout]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("cart-service").withCalls(
      restCall(Method.GET,  "/api/rest/carts",                              getCarts _),
      restCall(Method.POST, "/api/rest/carts",                              createCart _),
      restCall(Method.PUT,  "/api/rest/carts/:id/items/:id/quantity/:qtt",  setQuantityForCartItem _),
      restCall(Method.POST, "/api/rest/carts/:id/checkout",                 checkout _)
    )
      .withAutoAcl(true)
      .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
    // @formatter:on
  }
}

