package controllers

import anorm._
import java.util.Date
import jp.t2v.lab.play2.auth._
import models._
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._
import scala.xml.{XML => XML2}


object Settings extends Controller with AuthElement with AuthConfig {
  import play.api.Play.current

  val feedForm = Form(
      "url" -> text.verifying(nonEmpty)
    )

  def feed = authorizedAction(NormalUser) { user =>
    implicit request =>

      implicit val optionalUser = Option(user)
      val feeds = Feed.getAllForUser(user)

      Ok(views.html.settings.feed(feeds, feedForm))
  }

  def feedNew = authorizedAction(NormalUser) { user =>
    implicit request =>
      feedForm.bindFromRequest.fold(
        formWithErrors => Redirect(routes.Settings.feed),
        {
          case url => {
            try {
              val rss   = XML2.load(url)
              val name = (rss \ "channel" \ "title").text
              val site  = (rss \ "channel" \ "link").text
              val favicon = if (site != "") {
                  try {
                    val page = Jsoup.connect(site).header("User-Agent", "Mozilla/5.0").get()
                    page.select("link[rel^=shortcut]").first() match {
                      case null => Option(site + "/favicon.ico")
                      case shortcut => if (shortcut.attr("href").matches("^/.+") == true) {
                          Option(site + shortcut.attr("href"))
                        } else {
                          Option(shortcut.attr("href"))
                        }
                    }
                  } catch {
                    case e: HttpStatusException => {
                      println("Error (" + e.getStatusCode() + ") settings.feedNew " + e.getUrl())
                      None
                    }
                    case e: Throwable => {
                      println("Error (" + e.getClass +") settings.feedNew " + url)
                      None
                    }
                  }
                } else {
                  None
                }

              val feed = Feed.create(new Feed(NotAssigned, name, site, url, favicon))
              Subscription.create(new Subscription(user.id.get, feed.id.get, new Date()))

              Redirect(routes.Settings.feed).flashing("success" -> "Feed will be available within 15 minutes.")
            } catch {
              case e: Throwable => {
                println("Error (" + e.getClass +") settings.feedNew " + url)
                Redirect(routes.Settings.feed).flashing("error" -> "Feed invalid.")
              }
            }
          }
        }
      )
  }

  def feedUnsubscribe(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>
      Subscription.delete(user, Feed.getById(id).get)
      Redirect(routes.Settings.feed)
  }

}
