package ca.cbotek.item.impl.readside

import ca.cbotek.item.api.bundle.BundleItem
import ca.cbotek.item.api.cart.{Cart, CartBundle, CartItem}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * A Cart Repository is a singleton class wired to the main application and passed to the [[ca.cbotek.item.impl.ItemServiceImpl]]
  * in order for it to fetch information from the Cart readSide database.
  *
  * @param session a cassandra session allowing the car repository to connect to the database and to send statements.
  * @param ec execution context, used implicitly by [[Future]] in order to be dispatched in the Thread pool. Standard requirement
  *           for any implementation using Futures.
  */
class CartRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  /**
    * private method converting a [[Row]] to a [[Cart]].
    * As the types supported by the readSide database (here Cassandra) are not always convertible to Scala types, some choices
    * were made to simplify the implementation. Note that for [[Option]] and set of [[CartItem]] we are going to use the type
    * 'text'.
    *
    * More on cassandra column types
    * @see <a href="https://docs.datastax.com/en/cql/3.3/cql/cql_reference/cql_data_types_c.html">Cassandra Documentation.</a>
    *
    * Note that this method can fail if the cart id is not a valid [[java.util.UUID]]
    *
    * @return a Cart with informations extracted from a [[Row]]
    */
  private def rowToCart: Row => Cart = row =>
    Cart(
      id = row.getUUID("id"),
      user = row.getString("user"),
      items =
        Try(implicitly[Format[Set[CartItem]]].reads(Json.parse(row.getString("items"))).asOpt)
          .toOption
          .flatten
          .getOrElse(Set.empty[CartItem]),
      bundles =
        Try(implicitly[Format[Set[CartBundle]]].reads(Json.parse(row.getString("bundles"))).asOpt)
          .toOption
          .flatten
          .getOrElse(Set.empty[CartBundle]),
      price = Try(row.getString("price").toDouble).toOption
    )

  /**
    * public method fetching all carts available in the readSide database.
    *
    * @return return a list of [[Cart]]
    */
  def getCarts: Future[Iterable[Cart]] =
    session
      .selectAll("SELECT * from carts")
      .map(_.map(rowToCart))
}
