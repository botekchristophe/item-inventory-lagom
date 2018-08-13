package ca.cbotek.item.impl

import java.util.UUID

import ca.cbotek.item.api._
import ca.cbotek.item.api.bundle.{Bundle, BundleItem, BundleRequest, BundleRequestItem}
import ca.cbotek.item.api.cart._
import ca.cbotek.item.api.item.{Item, ItemRequest}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

object ItemServiceMock {
  final val itemRequest: ItemRequest = ItemRequest("testItem", "more than 2 char lengh", 1.0)
  final val bundleItem: BundleItem = BundleItem(1, Item(UUID.randomUUID(), "name", "more than 2 char lengh", 1.0))
  final val bundleRequest: BundleRequest = BundleRequest("bundleName", Iterable(BundleRequestItem(1, UUID.randomUUID())), 1)
}

class ItemServiceImplSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {
  import ItemServiceMock._

  lazy val server = ServiceTest.startServer(
    ServiceTest.defaultSetup.withCassandra
  ) { ctx =>
    new ItemApplication(ctx) with LocalServiceLocator
  }
  lazy val client = server.serviceClient.implement[ItemService]

  "Item Service" should {
    "Return empty Catalog" in {
      client.getCatalog.invoke().map { response =>
        response shouldBe Catalog(Set.empty[Item], Set.empty[Bundle])
      }
    }

    "Return empty cart list" in {
      client
        .getCarts
        .invoke()
        .map { response =>
        response shouldBe Iterable.empty[Cart]
      }
    }

    "Accept invoking create item endpoint" in {
      client
        .addItemToInventory
        .invoke(itemRequest)
        .map { response =>
        response.isRight shouldBe true
      }
    }

    "Reject invoking create item endpoint with negative price" in {
      client
        .addItemToInventory
        .invoke(itemRequest.copy(price = -1.0))
        .recover { case _: Throwable => Left("Deserialization failed") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Reject invoking create item endpoint with empty name" in {
      client
        .addItemToInventory
        .invoke(itemRequest.copy(name = ""))
        .recover { case _: Throwable => Left("Deserialization failed") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Reject invoking create bundle endpoint with negative average discount" in {
      client
        .addBundleToInventory
        .invoke(bundleRequest.copy(average_discount = -1.0))
        .recover { case _: Throwable => Left("Deserialization failed") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Reject invoking create bundle endpoint with empty name" in {
      client
        .addBundleToInventory
        .invoke(bundleRequest.copy(name = ""))
        .recover { case _: Throwable => Left("Deserialization failed") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Reject invoking delete item with randomUUID" in {
      client
        .removeItemFromInventory(UUID.randomUUID())
        .invoke
        .recover { case _: Throwable => Left("item not found") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Reject invoking delete bundle with randomUUID" in {
      client
        .removeBundleFromInventory(UUID.randomUUID())
        .invoke
        .recover { case _: Throwable => Left("bundle not found") }
        .map { response =>
        response.isLeft shouldBe true
      }
    }

    "Accept invoking create cart endpoint with empty Cart" in {
      client
        .createCart
        .invoke(CartCreateRequest("user", Set.empty[CartItem], Set.empty[CartBundle]))
        .flatMap { response =>
          response.isRight shouldBe true
        }
    }

    "Reject invoking setItemToCart cart with randomUUID" in {
      client
        .setItemsAndBundleToCart(UUID.randomUUID())
        .invoke(CartUpdateRequest(Set.empty, Set.empty))
        .recover { case _: Throwable => Left("bundle not found") }
        .map { response =>
          response.isLeft shouldBe true
        }
    }

    "Reject invoking checkout cart with randomUUID" in {
      client
        .checkoutCart(UUID.randomUUID())
        .invoke
        .recover { case _: Throwable => Left("bundle not found") }
        .map { response =>
          response.isLeft shouldBe true
        }
    }
  }

  override protected def beforeAll() = server

  override protected def afterAll() = server.stop()
}
