package ca.cbotek.item.impl.readside

import ca.cbotek.item.api.{Cart, CartItem}
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
          .getOrElse(Set.empty[CartItem])
    )

  def getCarts: Future[Iterable[Cart]] =
    session
      .selectAll("SELECT * from carts")
      .map(_.map(rowToCart))
}
