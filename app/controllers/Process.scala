package controllers

import anorm._
import java.net._
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import play.api._
import play.api.mvc._
import scala.collection.JavaConversions._

import models._
import parsers._

object Process extends Controller {
  import play.api.Play.current

  def LocalAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action { request =>
      if (request.remoteAddress == "127.0.0.1") {
        f(request)
      } else {
        Results.Unauthorized("Unauthorized.")
      }
    }
  }

  def index = LocalAction { request =>
    var newArticle: Int = 0

    val feeds = Feed.getAll

    for (feed <- feeds) {
      println(feed)
      newArticle += process(feed)
    }

    println("-")

    Ok(newArticle + " nouveaux articles")
  }


  def feed(id: Int) = LocalAction { request =>

    val feed = Feed.getById(id)

    feed match {
      case None => Ok("Feed not found")
      case Some(feed: Feed) => Ok(process(feed, true) + " refresh articles")
    }
  }


  def process(feed: Feed, force: Boolean = false): Int = {
    var newArticle: Int = 0
    val rss = scala.xml.XML.load(feed.url)
    val parser = Parser(feed.name)

    val simpleDateFormat = new SimpleDateFormat(parser.dateFormat, Locale.US)

    for (item <- rss \\ "item") {
      val title = (item \ "title").text
      val url   = (item \ "link").text
      val guid  = (item \ "guid").text

      val itemDb = Item.getByGuidForFeed(feed.id.get, guid)

      if (itemDb == None || force == true) {
        newArticle = newArticle+1

        val date = parser.getDate(item)

        val whitelist = Whitelist.basicWithImages()
        whitelist.addAttributes(":all", "width", "height")
        whitelist.addAttributes("iframe", "src")
        whitelist.addAttributes("embed", "type", "src", "allowfullscreen", "allowscriptaccess", "flashvars")
        whitelist.addAttributes("param", "name", "value")
        whitelist.addAttributes("object", "data")
        whitelist.addAttributes("div", "class")
        whitelist.addTags("div", "iframe", "object", "param", "embed", "section", "aside", "h1", "h2", "h3", "h4", "h5", "h6")

        try {
          val page = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0").get()
          val preContent = Jsoup.parse(parser.getContent(item, page))

          for(video <- preContent.select("iframe")) {
            video.removeAttr("height").removeAttr("width")
            video.wrap("<div class='videoWrapper'></div>")
          }

          for (element <- preContent.select("iframe")) {
            val host = new URL(element.attr("src")).getHost()
            if (host != "www.youtube.com" && host != "www.youtube-nocookie.com" && host != "player.vimeo.com") {
              element.remove()
            }
          }

          val javaUrl = new URL(url)
          val content = Jsoup.clean(preContent.html(), javaUrl.getProtocol() + "://" + javaUrl.getHost(), whitelist).replaceAll("<([^> ]+)( class=[^>]+)?>[\r\n\t ]*</\\1>", "")
          Item.createOrUpdate(new Item(NotAssigned, title, url, content, date, feed.id.get, guid))
        } catch {
          case e: HttpStatusException => println("Error (" + e.getStatusCode() + ") " + e.getUrl())
          case e: Throwable => println("Error (" + e.getClass +") " + url)
        }
      }
    }

    newArticle
  }

}
