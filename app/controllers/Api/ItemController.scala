package controllers
package api

import models._
import models.JsonImplicit._
import play.api._
import play.api.libs.json._
import play.api.mvc._

object ItemController extends Controller {
  import play.api.Play.current

  def get = Action { implicit request =>
    request.queryString.get("token") match {
        case Some(token) => {
            User.getByToken(token.mkString) match {
                case Some(user) => {
                    val items = Item.getAllForUser(user)

                    val itemsJson = for (item <- items.slice(0, 20)) yield {
                        val read    = item._3.map(_ => true).getOrElse(false)
                        val starred = item._4.map(_ => true).getOrElse(false)

                        Json.obj(
                          "id"      -> item._1.id,
                          "title"   -> item._1.title,
                          "url"     -> item._1.url,
                          "content" -> item._1.content,
                          "date"    -> item._1.date,
                          "feedId"  -> item._1.feedId,
                          "read"    -> read,
                          "starred" -> starred
                        )
                      }

                    val json = Json.obj("status" -> "ok", "items" -> itemsJson)

                    Ok(json)
                }
                case None => Ok(Json.obj("status" -> "ko", "error" -> "INVALID_TOKEN"))
            }
        }
        case None => Ok(Json.obj("status" -> "ko", "error" -> "NO_TOKEN"))
    }
  }

}
