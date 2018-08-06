package ca.cbotek.item.impl.readside

import java.util.UUID

import ca.cbotek.item.api.{Cart, CartItem}
import ca.cbotek.shared.ErrorResponse
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CartRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  private def rowToCart: Row => Cart = row =>
    Cart(
      id = row.getUUID("id"),
      user = row.getString("user"),
      items =
        Try(implicitly[Format[Set[CartItem]]].reads(Json.parse(row.getString("items"))).asOpt)
          .toOption
          .flatten
          .getOrElse(Set.empty[CartItem]),
      status = row.getString("status"),
      checkout_price = Try(row.getString("checkout_price").toDouble).toOption
    )

  def getCarts: Future[Iterable[Cart]] =
    session
      .selectAll("SELECT * from carts")
      .map(_.map(rowToCart))

  def getOneCart(id: UUID): Future[Either[ErrorResponse, Cart]] =
    session
    .selectOne("SELECT * from carts WHERE id = ?", id)
    .map(_.toRight(ErrorResponse(404, "Not found", s"Cart not found with id =$id")))
    .map(_.map(rowToCart))
}
