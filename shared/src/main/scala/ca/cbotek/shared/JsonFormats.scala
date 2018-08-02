package ca.cbotek.shared

import java.util.UUID

import play.api.libs.json._

import scala.language.implicitConversions
import scala.util.Try

object JsonFormats {

  /**
    * Helper to create a Json reads for [[scala.Enumeration]]
    * ``
    * object Foos extends Enumeration {
    *
    * val FOO, BAR = Value
    *
    * type Foo = Value
    *
    * implicit val format: Format[FOO] = enumFormat(Foos)
    * }
    * ``
    */
  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = Reads {
    case JsString(s) =>
      Try(JsSuccess(enum.withName(s).asInstanceOf[E#Value]))
        .getOrElse(JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not contain '$s'"))
    case _ =>
      JsError("String value expected")
  }
  def enumWrites[E <: Enumeration]: Writes[E#Value] = Writes(v => JsString(v.toString))
  def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = Format(enumReads(enum), enumWrites)

  /**
    * Json reads for [[java.util.UUID]]
    */
  implicit val uuidReads: Reads[UUID] =
    implicitly[Reads[String]]
      .collect(JsonValidationError("Invalid UUID"))(
        Function.unlift(str => Try(UUID.fromString(str)).toOption)
      )

  /**
    * Json writes for [[java.util.UUID]]
    */
  implicit val uuidWrites: Writes[UUID] = Writes(uuid => JsString(uuid.toString))

  /**
    * Json reads for [[scala.Either]]
    */
  implicit def eitherReads[A, B](implicit A: Reads[A], B: Reads[B]): Reads[Either[A, B]] =
    Reads[Either[A, B]] { json =>
      A.reads(json) match {
        case JsSuccess(value, path) => JsSuccess(Left(value), path)
        case JsError(e1) => B.reads(json) match {
          case JsSuccess(value, path) => JsSuccess(Right(value), path)
          case JsError(e2) => JsError(JsError.merge(e1, e2))
        }
      }
    }

  /**
    * Json writes for [[scala.Either]]
    */
  implicit def eitherWrites[A, B](implicit A: Writes[A], B: Writes[B]): Writes[Either[A, B]] =
    Writes[Either[A, B]] {
      case Left(a) => A.writes(a)
      case Right(b) => B.writes(b)
    }

  /**
    * Json format for [[scala.Either]]
    */
  implicit def eitherFormat[A, B](implicit A: Format[A], B: Format[B]): Format[Either[A, B]] =
    Format(eitherReads, eitherWrites)
}

