package parsers


import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.Jsoup
import org.jsoup.nodes._
import scala.collection.JavaConversions._
import scala.xml.Node

class Ubergizmo extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    val video = page.select(".youtube_subscription_link").first() match {
      case content: Element => {
        content.select("p:last-of-type").remove()
        content.html()
      }
      case null => ""
    }

    val content = page.select(".article").first() match {
      case content: Element => {
        content.select(".byline_container").remove()
        content.select("p[data-swiftype-index]").remove()
        content.select(".social-widgets").remove()
        content.select(".tag").remove()
        content.select(".post_taglist").remove()
        content.select(".indivsection").remove()
        content.select("#fbfaces").remove()
        content.html()
      }
      case null => ""
    }

    video + content
  }

}
