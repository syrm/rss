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

  def get(id: Int) = authorizedAction(NormalUser) { user =>
    implicit request =>
      import play.api.libs.json.Json

      Read.create(new Read(user.id.get, id, new Date()))

      val itemWithFeed = Item.getById(id)

      itemWithFeed match {
        case Some((item: Item, feed: Feed)) => Ok(views.html.item.get(item, feed))
        case None => Ok("Not found")
      }
  }

}
