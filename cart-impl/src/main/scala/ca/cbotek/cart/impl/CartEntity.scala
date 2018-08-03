package ca.cbotek.cart.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import org.slf4j.LoggerFactory

class CartEntity extends PersistentEntity {

  override type Command = CartCommand[_]
  override type Event = CartEvent
  override type State = Option[CartState]

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  override def initialState: Option[CartState] = None

  var currentState: Option[CartState] = initialState

  private val log = LoggerFactory.getLogger(classOf[CartEntity])

  override def behavior: Behavior = {
    Actions()
      .onEvent {
        case (_, state) => state
      }
  }
}

