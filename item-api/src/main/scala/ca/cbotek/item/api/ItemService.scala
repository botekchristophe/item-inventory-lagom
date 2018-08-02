package ca.cbotek.item.api

import java.util.UUID

import akka.NotUsed
import ca.cbotek.shared.ErrorResponse
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.{Environment, Mode}

trait ItemService extends Service {

  def getCatalog:               ServiceCall[NotUsed, Catalog]
  def addItem:                  ServiceCall[ItemRequest, Either[ErrorResponse, Item]]
  def addBundle:                ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]]
  def removeItem(id: UUID):     ServiceCall[NotUsed, Either[ErrorResponse, Item]]
  def removeBundle(id: UUID):   ServiceCall[NotUsed, Either[ErrorResponse, Bundle]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("item-service").withCalls(
      restCall(Method.GET,    "/api/rest/catalog",      getCatalog _),
      restCall(Method.POST,   "/api/rest/items",        addItem _),
      restCall(Method.DELETE, "/api/rest/items/:id",    removeItem _),
      restCall(Method.POST,   "/api/rest/bundles",      addBundle _),
      restCall(Method.DELETE, "/api/rest/bundles/:id",  removeBundle _)
    )
      .withAutoAcl(true)
      .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
    // @formatter:on
  }
}
