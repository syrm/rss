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

      val feeds = Feed.getAllForUser(user)
      val items = Item.getAllForUser(user)

      Ok(views.html.application.index(None, feeds, items))
  }


  def feed(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>

      implicit val optionalUser = Option(user)

      val feed  = Feed.getByIdForUser(id, user)
      val feeds = Feed.getAllForUser(user)
      val items = Item.getAllFromFeedForUser(id, user)

      Ok(views.html.application.index(feed, feeds, items))
  }

}
