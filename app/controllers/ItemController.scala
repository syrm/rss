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

  def get(id: Long) = authorizedAction(NormalUser) { implicit user =>
    implicit request =>
      import play.api.libs.json.Json

      Read.create(new Read(user.id.get, id, new Date()))

      val itemWithFeedAndBookmark = Item.getById(id)

      itemWithFeedAndBookmark match {
        case Some((item: Item, feed: Feed, bookmark: Option[Bookmark])) => Ok(views.html.item.get(item, feed, bookmark))
        case None => Ok("Not found")
      }
  }


  def star(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>
      import play.api.libs.json.Json

      Bookmark.create(new Bookmark(user.id.get, id, new Date()))

      Ok("OK")
  }


  def unstar(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>
      import play.api.libs.json.Json

      Bookmark.delete(user.id.get, id)

      Ok("OK")
  }

}
