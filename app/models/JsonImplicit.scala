package models

import anorm._
import play.api.libs.json._

object JsonImplicit {

  implicit val formatPk = new Format[Pk[Long]] {
    def writes(id: Pk[Long]): JsValue = JsNumber(id.get)
    def reads(id: JsValue): JsResult[Pk[Long]] =
      try {
        JsSuccess(Id(id.as[Long]))
      } catch {
        case e: IllegalArgumentException => JsError("Unable to parse Pk")
      }
  }

}