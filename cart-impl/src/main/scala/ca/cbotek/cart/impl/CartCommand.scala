package ca.cbotek.cart.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType

sealed trait CartCommand[R] extends ReplyType[R]
