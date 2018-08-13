package ca.cbotek.item.api

import java.util.UUID

import akka.NotUsed
import ca.cbotek.item.api.bundle.{Bundle, BundleRequest}
import ca.cbotek.item.api.cart.{Cart, CartCreateRequest, CartUpdateRequest}
import ca.cbotek.item.api.item.{Item, ItemRequest}
import ca.cbotek.shared.ErrorResponse
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.{Environment, Mode}

/**
  * The Item service.
  *
  * This service manage [[Item]] and [[Bundle]] creation as well as removal. The complete set of items and bundles available
  * in the application can be retrieved and expressed as a [[Catalog]].
  *
  * Furthermore, a user may create a [[Cart]] with a defined set of items and bundles that he wishes to purchase. A user
  * can update later the composition of the cart.
  *
  * Lastly, the user can request a cart checkout which will update the cart status and forbid further action to the cart.
  *
  */
trait ItemService extends Service {

  /**
    * REST api allowing a user to fetch the catalog of the application. See [[Catalog]] for more information about its fields.
    * The catalog is the current list of items and bundles available in the application.
    *
    * @return HTTP 200 status code with the current state of the application catalog.
    */
  def getCatalog: ServiceCall[NotUsed, Catalog]

  /**
    * REST api allowing a user to generate a random [[Catalog]].
    * If a previous catalog was generated in the past, calling this endpoint will reset the whole application
    * meaning carts, items and bundles information will be lost.
    *
    * @return HTTP 200 status code with the current state of the application catalog.
    */
  def generateCatalog: ServiceCall[NotUsed, Catalog]

  /**
    * Rest api allowing an administrator of the application to add a new item to the catalog.
    *
    * @return HTTP 200 status code with the newly created item info if the request was successful.
    *         HTTP 409 status code if an item allready exists with the same name.
    */
  def addItemToInventory: ServiceCall[ItemRequest, Either[ErrorResponse, Item]]

  /**
    * REST api allowing an administrator of the application to add a new bundle of items to the catalog.
    *
    * @return HTTP 200 status code with the bundle newly created if the request is successful.
    *         HTTP 404 status code if one or more item in the request do not exists.
    *         HTTP 409 status code if a bundle with the same name already exists.
    */
  def addBundleToInventory: ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]]

  /**
    * REST api allowing an administrator of the application to remove an existing item.
    *
    * @param id unique identifier of the item requested to be removed
    * @return HTTP 200 status code with the item removed if the request was successful.
    *         HTTP 404 status code if the item requested to be removed is part of an existing bundle.
    *                  in that case, the bundle needs to be removed first before removing the item.
    *         HTTP 404 status code if the item requested to be removed was not found in the inventory
    *
    */
  def removeItemFromInventory(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Item]]

  /**
    * REST api allowing an administrator to remove an existing bundle from the inventory.
    *
    * @param id unique identifier of the bundle requested to be removed.
    * @return HTTP 200 status code with the bundle removed if the request was successful.
    *         HTTP 404 status code if the bundle was not found in the inventory.
    */
  def removeBundleFromInventory(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Bundle]]

  /**
    * REST api allowing a administrator to see all the carts of the application. Some cart may be in use or checked out.
    *
    * @return HTTP 200 status code with a list of [[Cart]].
    */
  def getCarts: ServiceCall[NotUsed, Iterable[Cart]]

  /**
    * REST api allowing a user to create a cart based on a user name, a set of items as well as a quantity per item.
    *
    * @return HTTP 200 status code if the cart was created successfully.
    *         HTTP 404 status code if one or more items in the [[CartCreateRequest]] were not found in the inventory.
    *         HTTP 404 status code if one or more bundles in the [[CartCreateRequest]] were not found in the inventory.
    */
  def createCart: ServiceCall[CartCreateRequest, Either[ErrorResponse, Cart]]

  /**
    * REST api allowing a user to update an existing cart. This service works as a set/replace action where all the items
    * and bundles submitted in the request will replace the items and bundle already in the cart.
    *
    * @param cartId cart unique identifier
    * @return HTTP 200 status code with the updated car information, if the request is successful.
    *         HTTP 400 status code if the quantity requested is inferior to 0.
    *         HTTP 404 status code if the cart or the item was not found in the inventory.
    */
  def setItemsAndBundleToCart(cartId: UUID): ServiceCall[CartUpdateRequest, Either[ErrorResponse, Cart]]

  /**
    * REST api allowing a user to get optimization suggestion for an existing cart. The optimization suggestion might
    * contain some items and bundles the user did not added to his cart.
    *
    * @param id cart unique identifier
    * @return HTTP 200 status code if the cart was optimized successfully
    *         HTTP 404 status code if the cart was not found.
    */
  def optimizeCart(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Cart]]

  /**
    * REST api allowing a user to checkout. This action is not reversible and will prevent the user to perform any more
    * actions to the cart.
    *
    * @param id cart unique identifier
    * @return HTTP 200 status code if the cart was checked out successfully
    *         HTTP 404 status code if the cart was not found.
    */
  def checkoutCart(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Cart]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("item-service").withCalls(
      restCall(Method.GET,    "/api/rest/catalog",                            getCatalog _),
      restCall(Method.POST,   "/api/rest/catalog",                            generateCatalog _),
      restCall(Method.POST,   "/api/rest/items",                              addItemToInventory _),
      restCall(Method.DELETE, "/api/rest/items/:id",                          removeItemFromInventory _),
      restCall(Method.POST,   "/api/rest/bundles",                            addBundleToInventory _),
      restCall(Method.DELETE, "/api/rest/bundles/:id",                        removeBundleFromInventory _),
      restCall(Method.GET,    "/api/rest/carts",                              getCarts _),
      restCall(Method.POST,   "/api/rest/carts",                              createCart _),
      restCall(Method.PUT,    "/api/rest/carts/:id",                          setItemsAndBundleToCart _),
      restCall(Method.POST,   "/api/rest/carts/:id/optimize",                 optimizeCart _),
      restCall(Method.POST,   "/api/rest/carts/:id/checkout",                 checkoutCart _)
    )
      .withAutoAcl(true)
      .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
    // @formatter:on
  }
}
