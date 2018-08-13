package ca.cbotek.item.api.cart

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.{min, _}
import play.api.libs.json.{Reads, _}

/**
  * CartBundle is the representation of a Bundle from a Cart perspective. In this representation the bundle is represented
  * by its unique identifier and a quantity.
  *
  * @param bundleId bundle unique identifier.
  * @param quantity bundle quantity in this cart.
  */
case class CartBundle(bundleId: UUID,
                      quantity: Int)

object CartBundle {
  implicit val reads: Reads[CartBundle] = (
    (JsPath \ "bundleId").read[UUID] and
      (JsPath \ "quantity").read[Int](min(1))
    )(CartBundle.apply _)

  implicit val writes: Writes[CartBundle] = Json.writes[CartBundle]
  implicit val format: Format[CartBundle] = Format(reads, writes)
}
