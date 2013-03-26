package controllers

import anorm._
import java.util.Date
import jp.t2v.lab.play2.auth._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scala.xml.{XML => XML2}

import models._

object ItemController extends Controller with AuthElement with AuthConfig {
  import play.api.Play.current

  def read(itemId: Int) = authorizedAction(NormalUser) { user =>
    implicit request =>
      import play.api.libs.json.Json

      Read.create(new Read(user.id.get, itemId, new Date()))
      Ok(Json.obj("status" -> "OK"))
  }

}
