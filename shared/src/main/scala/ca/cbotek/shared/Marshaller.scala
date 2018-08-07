package ca.cbotek.shared

import com.lightbend.lagom.scaladsl.api.transport.{MessageProtocol, ResponseHeader}

import scala.collection.immutable
import scala.language.{implicitConversions, reflectiveCalls}
import scala.util.{Left, Right}

/**
  * Provide an automated marshalling for :
  * - `scala.util.Right` which will be marshaled as HTTP 200 response
  * - `scala.util.Left[ErrorResponse]` which will be marshaled as an Error 4xx or 5xx according to the error status code
  */
trait Marshaller {

  implicit def eitherMarshall[A]: Marshallable[Either[ErrorResponse, A]] = new Marshallable[Either[ErrorResponse, A]] {
    override def marshall(either: Either[ErrorResponse, A]): (ResponseHeader, Either[ErrorResponse, A]) =
      either match {
        case Left(e: ErrorResponse) =>
          (ResponseHeader(e.code, MessageProtocol.empty, immutable.Seq.empty[(String, String)]), Left(e))
        case right @ Right(_) =>
          (ResponseHeader.Ok, right)
      }
  }

  implicit def leftMarshall[A]: Marshallable[Left[ErrorResponse, A]] = new Marshallable[Left[ErrorResponse, A]] {
    override def marshall(l: Left[ErrorResponse, A]): (ResponseHeader, Left[ErrorResponse, A]) =
      (ResponseHeader(l.value.code, MessageProtocol.empty, immutable.Seq.empty[(String, String)]), l)
  }

  implicit def rightMarshall[A]: Marshallable[Right[ErrorResponse, A]] = new Marshallable[Right[ErrorResponse, A]] {
    override def marshall(r: Right[ErrorResponse, A]): (ResponseHeader, Right[ErrorResponse, A]) =
      (ResponseHeader.Ok, r)
  }

  implicit class MarshallOps[A](val a: A) {
    def marshall(implicit instance: Marshallable[A]): (ResponseHeader, A) =
      instance.marshall(a)
  }

  trait Marshallable[A] {
    def marshall(a: A): (ResponseHeader, A)
  }
}

