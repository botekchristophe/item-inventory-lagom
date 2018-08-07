package ca.cbotek.item.impl.optimizer

import ca.cbotek.item.api._
import ca.cbotek.item.impl.model.ItemInventoryState
import org.slf4j.LoggerFactory

object CartOptimizer {

  final val logger = LoggerFactory.getLogger(CartOptimizer.getClass)
  /**
    * Tail recursive method optimizing a CartCheckout based on a set of Bundle.
    * On each iteration the cart items are replaced by a bundle from the set of bundles.
    * The set of bundles is ordered by price and its head is used to try to optimize the cart.
    * If the most expensive bundle can be used, the set of item in the cart is updated and the bundle is
    * added to the cart bundles.
    * If the bundle cannot be used to replace some items, then the bundle is removed from the list for the
    * next iteration.
    * The recursive method stops when the set of bundles is empty.
    *
    * @param cart cart to be optimize.
    * @param bundles set of bundles available to optimize the cart.
    * @return an Optimized cart
    */
  def optimizeCart(cart: CartCheckout, bundles: Set[Bundle]): CartCheckout = {
    bundles
      .filterNot(bundle => bundleItemsNotInCartItems(bundle, cart))
      .toList.sortBy(_.price).reverse match {
      case Nil =>
        logger.info("Optimization done. No more bundle to try.")
        logger.info(s"Cart Items: ${cart.items.mkString("[", ", ", "]")}")
        logger.info(s"Cart Bundles: ${cart.bundles.mkString("[", ", ", "]")}")
        cart
      case all @ head :: tail =>
        logger.info(s"Trying to optimize with ${head.name} bundle - ${head.price} dollar(s).")
        logger.info(s"Bundle to go: ${all.size}")
        if (head.items.forall(bi => cartContainsEnoughItemsToUseBundle(bi, cart))) {
          val optimizedCart = cart.copy(
            items = removeBundleItemsFromCart(head, cart),
            bundles = addBundleToCart(head, cart)
          )
          logger.info("Success to optimize - keeping head")
          optimizeCart(optimizedCart, all.toSet)
        } else {
          logger.info("Failure to optimize - removing head - keeping tail.")
          optimizeCart(cart, tail.toSet)
        }
    }
  }

  /**
    * For this method, `intersect` is preferred from a combination of `forAll ... contains` due to computation speed.
    */
  private def bundleItemsNotInCartItems(bundle: Bundle, cart: CartCheckout): Boolean =
    bundle.items.map(_.item.id).intersect(cart.items.map(_.itemId)).size != bundle.items.map(_.item.id).size

  /**
    * This method checks whether the cart contains enough items compared to bundles items.
    */
  private def cartContainsEnoughItemsToUseBundle(bi: BundleItem, cart: CartCheckout): Boolean =
    cart.items.exists(ci => ci.itemId == bi.item.id && ci.quantity >= bi.quantity)

  /**
    * apply the selected bundle to the cart items and return the new set of items.
    */
  private def removeBundleItemsFromCart(bundle: Bundle, cart: CartCheckout): Set[CartItem] =
    cart
      .items
      .map(i => i.copy(quantity = i.quantity - bundle.items.find(_.item.id == i.itemId).map(_.quantity).getOrElse(0)))
      .filterNot(_.quantity <= 0)

  /**
    * apply the selected bundle to the cart bundle and return the set of bundles
    */
  private def addBundleToCart(bundle: Bundle, cart: CartCheckout): Set[CartBundle] =
    cart
      .bundles
      .find(_.bundleId == bundle.id)
      .fold[Set[CartBundle]](
      cart.bundles + CartBundle(bundle.id, 1))(
      cb => cart.bundles.filterNot(_.bundleId == cb.bundleId) + CartBundle(cb.bundleId, cb.quantity + 1))

  /**
    * Based on current cart and inventory, compute the cart total price.
    */
  def computeCartPrice(cart: CartCheckout, inv: ItemInventoryState): CartCheckout = {
    val itemsPrice: Double =
      cart.items.map(i => i.quantity.toDouble * inv.items.find(_.id == i.itemId).map(_.price).getOrElse(0.0)).sum
    val bundlesPrice: Double =
      cart.bundles.map(b => b.quantity.toDouble * inv.bundles.find(_.id == b.bundleId).map(_.price).getOrElse(0.0)).sum
    cart.copy(price = itemsPrice + bundlesPrice)
  }
}
