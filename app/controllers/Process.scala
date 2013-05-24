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
import play.libs.Akka._
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
    var nbFeed: Int = 0
    var newArticle: Int = 0

    val feeds = Feed.getAll
    val from = System.nanoTime()

    for (feed <- feeds) {
      nbFeed = nbFeed+1
      process(feed)
    }

    val end = System.nanoTime()
    val date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
    Logger.info(date + "\t" + ((end - from)/1000000000) + " sec\t" + nbFeed + " feeds\t" + newArticle + " items")

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
    var newArticle = 0

    try {
      val src = scala.io.Source.fromURL(feed.url)
      val rss = scala.xml.parsing.ConstructingParser.fromSource(src, false).document()
      val parser = Parser(feed.name)

      val idNode = if (feed.kind == FeedRss) {
          "guid"
        } else {
          "id"
        }

      val itemNode = if (feed.kind == FeedRss) {
          "item"
        } else {
          "entry"
        }

      val dateNode = if (feed.kind == FeedRss) {
          "pubDate"
        } else {
          "published"
        }

      val contentNode = if (feed.kind == FeedRss) {
          "description"
        } else {
          "content"
        }

      for (item <- rss \\ itemNode) {
        val title = (item \ "title").text
        val url   = if (feed.kind == FeedRss) {
            (item \ "link").text
          } else {
            val node = item \ "link"

            if (node.length > 0) {
              (node(0) \ "@href").text
            } else {
              ""
            }
          }

        val guid  = (item \ idNode).text

        val itemDb = Item.getByGuidForFeed(feed.id.get, guid)

        if (itemDb == None || force == true) {
          newArticle = newArticle+1

          val date = parser.getDate((item \ dateNode).text)

          val whitelist = Whitelist.basicWithImages()
          whitelist.addAttributes(":all", "width", "height")
          whitelist.addAttributes("iframe", "src")
          whitelist.addAttributes("embed", "type", "src", "allowfullscreen", "allowscriptaccess", "flashvars")
          whitelist.addAttributes("param", "name", "value")
          whitelist.addAttributes("object", "data")
          whitelist.addAttributes("div", "class")
          whitelist.addTags("div", "iframe", "object", "param", "embed", "section", "aside", "h1", "h2", "h3", "h4", "h5", "h6")

          try {
            val preContent = if (parser.hasFullContent) {
                val page = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0").get()
                Jsoup.parse(parser.getContent(item, page))
              } else {
                Jsoup.parse((item \ contentNode).text)
              }


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
            case e: HttpStatusException => Logger.error("HttpStatusException\t" + feed.id + "\t" + e.getStatusCode() + "\t" + url)
            case e: Throwable => {
              Logger.error("Throwable\t" + feed.id + "\t" + e.getClass +"\t" + url)
              Logger.error("\t" + item)
            }
          }
        }
      }

      Feed.updateLastUpdate(feed.id.get)
    } catch {
      case e: Throwable => {
        Feed.updateLastError(feed.id.get, e.toString())
      }
    }

    newArticle
  }

}
