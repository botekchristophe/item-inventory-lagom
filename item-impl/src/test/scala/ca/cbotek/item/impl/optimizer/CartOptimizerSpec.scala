package ca.cbotek.item.impl.optimizer

import org.scalatest.{Matchers, WordSpec}

class CartOptimizerSpec extends WordSpec with Matchers {

  "Cart optimizer" should {
    "Compute cart with random items price" in {
      CartOptimizer.computeCartPrice(Mock.randomCart, Mock.inventory).price shouldBe Mock.randomCart.price
    }

    "Compute empty cart" in {
      CartOptimizer.computeCartPrice(Mock.emptyCart, Mock.inventory).price shouldBe 0.0
    }

    "Find an optimized price smaller than starting price" in {
      CartOptimizer.optimizeCart(Mock.randomCart, Mock.inventory.bundles).price <= Mock.nonOptimizedPrice shouldBe true
    }
  }
}
