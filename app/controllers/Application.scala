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

      if (feeds.length == 0) {
        Ok(views.html.application.welcome())
      } else {
        Ok(views.html.application.index("all", None, feeds, items))
      }
  }


  def feed(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>

      implicit val optionalUser = Option(user)

      val feed  = Feed.getByIdForUser(id, user)
      val feeds = Feed.getAllForUser(user)
      val items = Item.getAllFromFeedForUser(id, user)

      feed match {
        case None => Ok(views.html.application.index("all", None, feeds, items))
        case feed => Ok(views.html.application.index("feed", feed, feeds, items))
      }
  }


  def starred = authorizedAction(NormalUser) { user =>
    implicit request =>

      implicit val optionalUser = Option(user)

      val feeds = Feed.getAllForUser(user)
      val items = Item.getAllStarredForUser(user)

      Ok(views.html.application.index("starred", None, feeds, items))
  }

}
