package controllers

import anorm._
import java.net._
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
              val xmlFeed = XML2.load(url)

              // atom feed
              val feedKind = if ((xmlFeed \ "channel").length == 0) {
                FeedAtom
              } else { // rss feed
                FeedRss
              }

              val name = if (feedKind == FeedAtom) {
                  (xmlFeed \ "title").text
                } else {
                  (xmlFeed \ "channel" \ "title").text
                }
              val site = if (feedKind == FeedAtom) {
                  val node = xmlFeed \ "link"

                  if (node.length > 0) {
                    (node(0) \ "@href").text
                  } else {
                    ""
                  }
                } else {
                  (xmlFeed \ "channel" \ "link").text
                }

              val favicon = (if (site != "") {
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
                      Logger.error("HttpStatusException\tsettings.feedNew\t" + e.getStatusCode() + "\t" + url)
                      None
                    }
                    case e: Throwable => {
                      Logger.error("Throwable\tsettings.feedNew\t" + e.getClass +"\t" + url)
                      None
                    }
                  }
                } else {
                  None
                }) match {
                  case None => None
                  case Some(favicon) => {
                    val result = new URL(favicon).openConnection().asInstanceOf[HttpURLConnection].getResponseCode()

                    if (result == 200) {
                      Option(favicon)
                    } else {
                      None
                    }
                  }
                }

              val feed = Feed.create(new Feed(NotAssigned, name, site, url, favicon, None, feedKind))
              Subscription.create(new Subscription(user.id.get, feed.id.get, new Date()))

              Redirect(routes.Settings.feed).flashing("success" -> "Feed will be available within few minutes.")
            } catch {
              case e: Throwable => {
                Logger.error("Throwable\tsettings.feedNew\t" + e.getClass +"\t" + url)
                Redirect(routes.Settings.feed).flashing("error" -> "Feed invalid.")
              }
            }
          }
        }
      )
  }

  def feedUnsubscribe(id: Long) = authorizedAction(NormalUser) { user =>
    implicit request =>
      Feed.getById(id) match {
        case Some(feed) => Subscription.delete(user, feed)
        case None => None
      }

      Redirect(routes.Settings.feed)
  }

}
