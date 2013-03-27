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
import scala.xml.{XML => XML2}

import models._
import parsers._

object Process extends Controller {
  import play.api.Play.current

  def index = Action {
    var newArticle: Int = 0

    val feeds = Feed.getAll

    for (feed <- feeds) {
      println(feed)
      newArticle += process(feed)
    }

    println("-")

    Ok(newArticle + " nouveaux articles")
  }


  def feed(id: Int) = Action {
    val feed = Feed.getById(id)

    feed match {
      case None => Ok("Feed not found")
      case Some(feed: Feed) => Ok(process(feed, true) + " refresh articles")
    }
  }


  def process(feed: Feed, force: Boolean = false): Int = {
    var newArticle: Int = 0
    val rss = XML2.load(feed.url)
    val parser = Parser(feed.name)

    val simpleDateFormat = new SimpleDateFormat(parser.dateFormat, Locale.US)

    for (item <- rss \\ "item") {
      val title = (item \ "title").text
      val url   = (item \ "link").text

      println(url)

      val itemDb = Item.getByUrl(url)

      if (itemDb == None || force == true) {
        newArticle = newArticle+1

        val date = parser.getDate(item)

        val whitelist = Whitelist.basicWithImages()
        whitelist.addAttributes(":all", "width", "height")
        whitelist.addAttributes("iframe", "src")
        whitelist.addAttributes("embed", "type", "src", "allowfullscreen", "allowscriptaccess", "flashvars")
        whitelist.addAttributes("param", "name", "value")
        whitelist.addTags("div", "iframe", "object", "param", "embed", "section", "aside")

        try {
          val page = Jsoup.connect(url).header("User-Agent", "Mozilla/5.0").get()
          val content = Jsoup.clean(parser.getContent(item, page), whitelist)

          Item.createOrUpdate(new Item(NotAssigned, title, url, content, date, feed.id.get))
        } catch {
          case e: HttpStatusException => println("Error (" + e.getStatusCode() + ") " + e.getUrl())
          case e => println("Error (" + e.getClass +") " + url)
        }
      }
    }

    newArticle
  }

}
