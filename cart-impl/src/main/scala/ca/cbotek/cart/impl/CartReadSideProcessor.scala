package ca.cbotek.cart.impl

import akka.Done
import ca.cbotek.cart.api.CartItem
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import org.slf4j.LoggerFactory
import play.api.libs.json.Format

import scala.concurrent.{ExecutionContext, Future}

class CartReadSideProcessor(readSide: CassandraReadSide, session: CassandraSession)
                           (implicit ec: ExecutionContext) extends ReadSideProcessor[CartEvent] {

  private val log = LoggerFactory.getLogger(classOf[CartReadSideProcessor])

  private var insertCartStatement: PreparedStatement = _
  private var updateCartItemsStatement: PreparedStatement = _
  private var updateCartStatusStatement: PreparedStatement = _

  def buildHandler: ReadSideHandler[CartEvent] = {
    readSide.builder[CartEvent]("cartOffset")
      .setGlobalPrepare(createTable)
      .setPrepare { tag => prepareStatements()}
      .setEventHandler[CartCreated](cartCreated)
      .setEventHandler[CartItemsUpdated](cartItemsUpdated)
      .setEventHandler[CartCheckedout](cartCheckedOut)
      .build()
  }

  private def createTable(): Future[Done] = {
    for {
      _ <- session.executeCreateTable(
        """
          |CREATE TABLE IF NOT EXISTS carts (
          |   id uuid, user text, items text, status text,
          |   PRIMARY KEY (id)
          |   )
        """.stripMargin)
    } yield Done
  }

  private def prepareStatements(): Future[Done] = {
    for {
      insertCart <- session.prepare(
        """
          |INSERT INTO carts
          |(id, user, items, status)
          |VALUES (?, ?, ?, ?)
        """.stripMargin
      )

      updateCartItems <- session.prepare(
        """
          |UPDATE carts
          |SET status = ?
          |WHERE id = ?
        """.stripMargin
      )

      updateCartStatus <- session.prepare(
        """
          |UPDATE carts
          |SET status = ?
          |WHERE id = ?
        """.stripMargin
      )
    } yield {
      insertCartStatement = insertCart
      updateCartItemsStatement = updateCartItems
      updateCartStatusStatement = updateCartStatus
      Done
    }
  }

  private def cartCreated(e: EventStreamElement[CartCreated]): Future[List[BoundStatement]] = {
    Future.successful {
      val c = e.event
      List(insertCartStatement.bind(
        c.id,
        c.user,
        implicitly[Format[Set[CartItem]]].writes(c.items),
        CartStatuses.IN_USE.toString
      ))
    }
  }

  private def cartItemsUpdated(e: EventStreamElement[CartItemsUpdated]): Future[List[BoundStatement]] = {
    Future.successful {
      List(updateCartItemsStatement.bind(
        implicitly[Format[Set[CartItem]]].writes(e.event.items),
        e.event.id
      ))
    }
  }

  private def cartCheckedOut(e: EventStreamElement[CartCheckedout]): Future[List[BoundStatement]] = {
    Future.successful {
      val c = e.event
      List(updateCartStatusStatement.bind(
        CartStatuses.CHECKEDOUT,
        c.id
      ))
    }
  }

  override def aggregateTags: Set[AggregateEventTag[CartEvent]] = CartEvent.Tag.allTags
}
