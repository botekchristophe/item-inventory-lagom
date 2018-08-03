package ca.cbotek.item.impl

import akka.Done
import ca.cbotek.shared.ErrorResponse
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.slf4j.LoggerFactory

class ItemInventoryEntity extends PersistentEntity {

  override type Command = ItemCommand[_]
  override type Event = ItemEvent
  override type State = Option[ItemInventoryState]

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  override def initialState: Option[ItemInventoryState] = None

  var currentState: Option[ItemInventoryState] = initialState

  private val log = LoggerFactory.getLogger(classOf[ItemInventoryEntity])

  override def behavior: Behavior = {
    case None => unCreated
    case Some(_) => created
  }

  private def unCreated: Actions =
    Actions()
      .onCommand[AddItem, Either[ErrorResponse, Done]] { replyNotImplemented }
      .onCommand[DeleteItem, Either[ErrorResponse, Done]] { replyNotImplemented }
      .onEvent {
        case (_, state) => state
      }

  private def created: Actions =
    Actions()
      .onCommand[AddItem, Either[ErrorResponse, Done]] { replyNotImplemented }
      .onCommand[DeleteItem, Either[ErrorResponse, Done]] { replyNotImplemented }
      .onEvent {
        case (_, state) => state
      }

  private def replyNotFound[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(404, "Not Found", "Item not found.")))
      ctx.done
  }

  private def replyConflict[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(409, "Conflict", "Item already exists.")))
      ctx.done
  }

  private def replyNotImplemented[R]: OnCommandHandler[Either[ErrorResponse, R]] = {
    case (_, ctx, _) =>
      ctx.reply(Left(ErrorResponse(501, "Not Implemented", "Will be implemented soon.")))
      ctx.done
  }
}
