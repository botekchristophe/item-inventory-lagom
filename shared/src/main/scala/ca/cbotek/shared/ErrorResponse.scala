package ca.cbotek.shared

import play.api.libs.json.{Format, Json}

/**
  * Common model to display an error.
  *
  * @param code status code as an integer
  * @param error status code as a string
  * @param message Short description of the error that occurred.
  */
case class ErrorResponse(code: Int, error: String, message: String)

object ErrorResponse {
  implicit val format: Format[ErrorResponse] = Json.format[ErrorResponse]
}
