package ca.cbotek.shared

import java.util.UUID

import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{Format, Json}
import JsonFormats._

object Mock {
  case class UUIDTest(id: UUID)
  object UUIDTest {
    implicit val format: Format[UUIDTest] = Json.format[UUIDTest]
  }
  final val testObject: UUIDTest = UUIDTest(UUID.randomUUID)
  final val testObjectjson: String = s"""{"id":"${testObject.id.toString}"}"""

  case class Response(name: String, description: String)
  object Response {
    implicit val format: Format[Response] = Json.format[Response]
  }
  final val responseObject = Response("name", "description")
  final val eitherResponseObject: Either[ErrorResponse, Response] = Right(responseObject)
  final val errorResponse = ErrorResponse(999, "Error", "Test error")
  final val eitherErrorResponse: Either[ErrorResponse, Response] = Left(errorResponse)

  case object TestObject {
    implicit val format: Format[TestObject.type] = singletonFormat(TestObject)
  }
  final val testObjectJson = """{"value":"TestObject$"}"""
}


class JsonFormatsSpec extends WordSpec with Matchers {
  import Mock._

  "JsonFormats" should {

    "Serialize/Deserialize UUIDs" in {
      UUIDTest.format.writes(testObject).toString() shouldBe testObjectjson
      UUIDTest.format.reads(Json.parse(testObjectjson)).get shouldBe testObject
    }

    "Serialize/Deserialize Either" in {

      val responseStr = Response.format.writes(responseObject).toString
      val rightResponseStr = implicitly[Format[Either[ErrorResponse, Response]]].writes(eitherResponseObject).toString()
      responseStr shouldBe rightResponseStr

      val errorStr = ErrorResponse.format.writes(errorResponse).toString
      val leftErrorStr = implicitly[Format[Either[ErrorResponse, Response]]].writes(eitherErrorResponse).toString()
      errorStr shouldBe leftErrorStr

      val response: Either[ErrorResponse, Response] = Right(Response.format.reads(Json.parse(responseStr)).get)
      val rightResponse = implicitly[Format[Either[ErrorResponse, Response]]].reads(Json.parse(rightResponseStr)).get
      response shouldBe rightResponse

      val error: Either[ErrorResponse, Response] = Left(ErrorResponse.format.reads(Json.parse(errorStr)).get)
      val leftError = implicitly[Format[Either[ErrorResponse, Response]]].reads(Json.parse(leftErrorStr)).get
      error shouldBe leftError
    }

    "Serialize/Deserialize Singleton" in {
      TestObject.format.reads(Json.parse(testObjectJson)).get shouldBe TestObject
      TestObject.format.writes(TestObject).toString() shouldBe testObjectJson
    }
  }
}
