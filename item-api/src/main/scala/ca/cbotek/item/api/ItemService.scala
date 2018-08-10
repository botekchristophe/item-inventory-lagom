package ca.cbotek.item.api

import java.util.UUID

import akka.NotUsed
import ca.cbotek.shared.ErrorResponse
import ca.cbotek.shared.JsonFormats._
import com.lightbend.lagom.scaladsl.api.deser.DefaultExceptionSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.{Environment, Mode}

/**
  * The Item service.
  *
  * This service manage items and bundles creation as well as removal. The complete set of items and bundles available
  * in the application can be retrieved and expressed as a Catalog.
  *
  * Furthermore, when a user request to buy some items, he can create a Cart which will contain the items requested. Later,
  * the same user can update the quantity of an item he would like to purchase or set a new items in the cart. Once the user
  * is done browsing and adding items to the cart he can request a cart optimization which will show him what bundles and
  * item combination will drop down the total checkout price of his cart.
  *
  * Lastly, the user can request a cart checkout which will update the cart status and forbid further action to the cart.
  *
  */
trait ItemService extends Service {

  /**
    * Rest api allowing a user to fetch the catalog of the application. See [[Catalog]] for more information about its fields.
    * The catalog is the current list of items and bundles available in the application.
    *
    * @return HTTP 200 status code with the current state of the application catalog.
    */
  def getCatalog:                                                         ServiceCall[NotUsed, Catalog]

  /**
    * Rest api allowing an administrator of the application to add a new item to the catalog.
    *
    * @return HTTP 200 status code with the newly created item info if the request was successful.
    *         HTTP 409 status code if an item allready exists with the same name.
    */
  def addItem:                                                            ServiceCall[ItemRequest, Either[ErrorResponse, Item]]

  /**
    * REST api allowing an administrator of the application to add a new bundle of items to the catalog.
    *
    * @return HTTP 200 status code with the bundle newly created if the request is successful.
    *         HTTP 404 status code if one or more item in the request do not exists.
    *         HTTP 409 status code if a bundle with the same name already exists.
    */
  def addBundle:                                                          ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]]

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
  def removeItem(id: UUID):                                               ServiceCall[NotUsed, Either[ErrorResponse, Item]]

  /**
    * REST api allowing an administrator to remove an existing bundle from the inventory.
    *
    * @param id unique identifier of the bundle requested to be removed.
    * @return HTTP 200 status code with the bundle removed if the request was successful.
    *         HTTP 404 status code if the bundle was not found in the inventory.
    */
  def removeBundle(id: UUID):                                             ServiceCall[NotUsed, Either[ErrorResponse, Bundle]]

  /**
    * REST api allowing a administrator to see all the carts of the application. Some cart may be in use or checked out.
    *
    * @return HTTP 200 status code with a list of [[Cart]].
    */
  def getCarts:                                                           ServiceCall[NotUsed, Iterable[Cart]]

  /**
    * REST api allowing a user to create a cart based on a user name, a set of items as well as a quantity per item.
    *
    * @return HTTP 200 status code if the cart was created successfully.
    *         HTTP 404 status code if one or more items in the [[CartRequest]] were not found in the inventory.
    */
  def createCart:                                                         ServiceCall[CartRequest, Either[ErrorResponse, Cart]]

  /**
    * REST api allowing a user to update an existing cart. This api works as a 'set' action were if an item is not allready part
    * of the cart, it will be added with the quantity set in the request. If a quantity is set to 0 then the item will be removed
    * from the cart altogether.
    *
    * @param cartId car unique identifier
    * @param itemId item unique identifier
    * @param quantity targeted quantity of the item
    * @return HTTP 200 status code with the updated car information, if the request is successful
    *         HTTP 400 status code if the quantity requested is inferior to 0.
    *         HTTP 404 is the cart or the item was not found in the inventory
    */
  def setQuantityForCartItem(cartId: UUID, itemId: UUID, quantity: Int):  ServiceCall[NotUsed, Either[ErrorResponse, Cart]]

  /**
    * REST api allowing a user to checkout an existing cart. Performing this action will update irreversibly the cart status
    * to 'CHECKED_OUT'. The response of the api will be to return an optimized cart.
    *
    * @param id car unique identifier
    * @return HTTP 200 status code if the cart was optimized successfully
    *         HTTP 404 status code if the cart was not found.
    */
  def checkout(id: UUID):                                                 ServiceCall[NotUsed, Either[ErrorResponse, CartCheckout]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("item-service").withCalls(
      restCall(Method.GET,    "/api/rest/catalog",                            getCatalog _),
      restCall(Method.POST,   "/api/rest/items",                              addItem _),
      restCall(Method.DELETE, "/api/rest/items/:id",                          removeItem _),
      restCall(Method.POST,   "/api/rest/bundles",                            addBundle _),
      restCall(Method.DELETE, "/api/rest/bundles/:id",                        removeBundle _),
      restCall(Method.GET,    "/api/rest/carts",                              getCarts _),
      restCall(Method.POST,   "/api/rest/carts",                              createCart _),
      restCall(Method.PUT,    "/api/rest/carts/:id/items/:id/quantity/:qtt",  setQuantityForCartItem _),
      restCall(Method.POST,   "/api/rest/carts/:id/checkout",                 checkout _)
    )
      .withAutoAcl(true)
      .withExceptionSerializer(new DefaultExceptionSerializer(Environment.simple(mode = Mode.Prod)))
    // @formatter:on
  }
}
