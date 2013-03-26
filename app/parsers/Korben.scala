package parsers

import org.jsoup.nodes._
import scala.xml.Node

class Korben extends Default {

  override def dateFormat = "EEE, d MMM yyyy HH:mm:ss Z"

  override def getContent(item: Node, page: Document) = {
    page.select("div.post-content").first() match {
      case content: Element => {
        content.select("div.robots-nocontent").remove()
        content.select("div.ribbon").remove()
        content.select("p:last-of-type").remove()
        content.select("div:last-of-type").remove()

        content.html()
      }
      case null => ""
    }
  }

}
