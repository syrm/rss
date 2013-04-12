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
        case Some((item: Item, feed: Feed, bookmark: Option[Bookmark])) => {

          val bookmarkBoolean = bookmark.map(_ => true).getOrElse(false)
          val json = Json.obj(
            "status" -> "ok",
            "item" ->
              Json.obj(
                "id"      -> item.id.get,
                "title"   -> item.title,
                "url"     -> item.url,
                "content" -> item.content
              ),
            "feed" ->
              Json.obj(
                "id"      -> feed.id.get,
                "name"    -> feed.name,
                "favicon" -> feed.favicon,
                "unread"  -> feed.unread
              ),
            "bookmark" -> bookmarkBoolean
            )

          Ok(json)
        }
        case None => Ok(Json.obj("status" -> "ko"))
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
