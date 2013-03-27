package controllers

import anorm._
import java.util.Date
import jp.t2v.lab.play2.auth._
import models._
import play.api._
import play.api.mvc._
import scala.xml.{XML => XML2}


object Application extends Controller with AuthElement with AuthConfig {
  import play.api.Play.current

  def index = authorizedAction(NormalUser) { user =>
    implicit request =>

      implicit val optionalUser = Option(user)
      val items = Item.getAllForUser(user)
      Read.create(new Read(user.id.get, items(0)._1.id.get, new Date()))

      Ok(views.html.application.index(items))
  }

}
