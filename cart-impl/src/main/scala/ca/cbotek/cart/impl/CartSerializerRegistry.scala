package ca.cbotek.cart.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object CartSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CreateCart],
    JsonSerializer[SetItemToCart],
    JsonSerializer[CheckoutCart],

    JsonSerializer[CartCreated],
    JsonSerializer[CartItemsUpdated],
    JsonSerializer[CartCheckedout],

    JsonSerializer[CartState]
  )
}


