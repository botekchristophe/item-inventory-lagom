package ca.cbotek.item.impl

import java.util.UUID

import akka.{Done, NotUsed}
import ca.cbotek.item.api._
import ca.cbotek.item.api.bundle.{Bundle, BundleRequest}
import ca.cbotek.item.api.cart.{Cart, CartCreateRequest, CartUpdateRequest}
import ca.cbotek.item.api.item.{Item, ItemRequest}
import ca.cbotek.item.impl.ServiceErrors._
import ca.cbotek.item.impl.command._
import ca.cbotek.item.impl.entity.{CartEntity, ItemInventoryEntity}
import ca.cbotek.item.impl.readside.CartRepository
import ca.cbotek.shared.{ErrorResponse, Marshaller}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

class ItemServiceImpl(registry: PersistentEntityRegistry,
                      cartRepository: CartRepository)
                     (implicit ec: ExecutionContext) extends ItemService with Marshaller {

  /**
    * [[ItemInventoryEntity]] is a singleton and its ID is defined in application.conf file.
    *
    * @return Returns a reference to the entity holding the inventory.
    */
  private def refForInventory = registry.refFor[ItemInventoryEntity](itemInventoryEntityName)

  /**
    * [[CartEntity]] is a persistence entity of a Cart. Each user cart state is held by a different entity.
    * In order to access a cart entity, one needs to provide the cart id as a [[UUID]].
    * The cart unique id and the cart entity will both use the exact same [[UUID]] in the write and read sides.
    *
    * @param id the cart id
    * @return a reference on a cart entity defined by its [[UUID]]
    */
  private def refForCart(id: UUID) = registry.refFor[CartEntity](id.toString)

  override def getCatalog: ServiceCall[NotUsed, Catalog] =
    ServiceCall(_ =>
      refForInventory
        .ask(GetInventory)
        .map(inventory => Catalog(inventory.items, inventory.bundles))
    )

  /**
    * REST api allowing a user to generate a random [[Catalog]].
    * If a previous catalog was generated in the past, calling this endpoint will reset the whole application
    * meaning carts, items and bundles information will be lost.
    *
    * @return HTTP 200 status code with the current state of the application catalog.
    */
  override def generateCatalog: ServiceCall[NotUsed, Catalog] = ???

  /**
    * Rest api allowing an administrator of the application to add a new item to the catalog.
    *
    * @return HTTP 200 status code with the newly created item info if the request was successful.
    *         HTTP 409 status code if an item allready exists with the same name.
    */
  override def addItemToInventory: ServiceCall[ItemRequest, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddItem(UUID.randomUUID(), request.name, request.description, request.price))
        .map(_.marshall)
    )

  /**
    * REST api allowing an administrator of the application to add a new bundle of items to the catalog.
    *
    * @return HTTP 200 status code with the bundle newly created if the request is successful.
    *         HTTP 404 status code if one or more item in the request do not exists.
    *         HTTP 409 status code if a bundle with the same name already exists.
    */
  override def addBundleToInventory: ServiceCall[BundleRequest, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, request) =>
      refForInventory
        .ask(AddBundle(UUID.randomUUID(), request.name, request.items, request.average_discount))
        .map(_.marshall)
    )

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
  override def removeItemFromInventory(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Item]] =
    ServerServiceCall((_, _) =>
      refForInventory.ask(DeleteItem(id))
        .map(_.marshall)
    )

  /**
    * REST api allowing an administrator to remove an existing bundle from the inventory.
    *
    * @param id unique identifier of the bundle requested to be removed.
    * @return HTTP 200 status code with the bundle removed if the request was successful.
    *         HTTP 404 status code if the bundle was not found in the inventory.
    */
  override def removeBundleFromInventory(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Bundle]] =
    ServerServiceCall((_, _) =>
      refForInventory
        .ask(DeleteBundle(id))
        .map(_.marshall)
    )

  /**
    * REST api allowing a administrator to see all the carts of the application. Some cart may be in use or checked out.
    *
    * @return HTTP 200 status code with a list of [[Cart]].
    */
  override def getCarts: ServiceCall[NotUsed, Iterable[Cart]] =
    ServiceCall(_ => cartRepository.getCarts)

  /**
    * REST api allowing a user to create a cart based on a user name, a set of items as well as a quantity per item.
    *
    * @return HTTP 200 status code if the cart was created successfully.
    *         HTTP 404 status code if one or more items in the [[CartCreateRequest]] were not found in the inventory.
    *         HTTP 404 status code if one or more bundles in the [[CartCreateRequest]] were not found in the inventory.
    */
  override def createCart: ServiceCall[CartCreateRequest, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, request) =>
      checkItemsAndBundlesExists(request.items.map(_.itemId), request.bundles.map(_.bundleId))
        .flatMap(_.fold(
          e => Future.successful(Left(e)),
          _ => {
            val cartId: UUID = UUID.randomUUID()
            refForCart(cartId).ask(CreateCart(cartId, request.user, request.items, request.bundles))}))
        .map(_.marshall)
    )

  /**
    * REST api allowing a user to update an existing cart. This service works as a set/replace action where all the items
    * and bundles submitted in the request will replace the items and bundle already in the cart.
    *
    * @param cartId cart unique identifier
    * @return HTTP 200 status code with the updated car information, if the request is successful.
    *         HTTP 400 status code if the quantity requested is inferior to 0.
    *         HTTP 404 status code if the cart or the item was not found in the inventory.
    */
  override def setItemsAndBundleToCart(cartId: UUID): ServiceCall[CartUpdateRequest, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, request) =>
      checkItemsAndBundlesExists(request.items.map(_.itemId), request.bundles.map(_.bundleId))
        .flatMap(_.fold(
          e => Future.successful(Left(e)),
          _ => refForCart(cartId).ask(UpdateCart(cartId, request.items, request.bundles))))
        .map(_.marshall)
    )

  /**
    * REST api allowing a user to get optimization suggestion for an existing cart. The optimization suggestion might
    * contain some items and bundles the user did not added to his cart.
    *
    * @param id cart unique identifier
    * @return HTTP 200 status code if the cart was optimized successfully
    *         HTTP 404 status code if the cart was not found.
    */
  override def optimizeCart(id: UUID): ServiceCall[NotUsed, Either[ServiceError, Cart]] = ???

  /**
    * REST api allowing a user to checkout. This action is not reversible and will prevent the user to perform any more
    * actions to the cart.
    *
    * @param id cart unique identifier
    * @return HTTP 200 status code if the cart was checked out successfully
    *         HTTP 404 status code if the cart was not found.
    */
  override def checkoutCart(id: UUID): ServiceCall[NotUsed, Either[ErrorResponse, Cart]] =
    ServerServiceCall((_, _) =>
      refForCart(id).ask(CheckoutCart(id)).map(_.marshall)
    )

  /**
    * private method which can be used to check if a set of Item identifier exists in the item inventory.
    *
    * @param itemIds set of item ids to check
    * @param bundlesIds set of bundle ids to check
    * @return if all ids are effectively part of the inventory, the method will return a [[scala.util.Right]] containing
    *         [[Done]]
    *         if one or more ids is not found in the inventory, the method will return a [[scala.util.Left]] containing
    *         a [[ServiceError]]
    */
  private def checkItemsAndBundlesExists(itemIds: Set[UUID], bundlesIds: Set[UUID]): Future[Either[ServiceError, Done]] =
    refForInventory
      .ask(GetInventory)
      .map(inventory =>
        if (inventory.items.map(_.id).intersect(itemIds).size == itemIds.size &&
          inventory.bundles.map(_.id).intersect(bundlesIds).size == itemIds.size) {
          Right(Done)
        } else {
          Left(ItemsNotFoundInInventory)
        })
}
