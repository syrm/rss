package parsers

import org.jsoup.nodes._
import scala.xml.Node

class Webification extends Default {

  override def getContent(item: Node, page: Document) = {
    page.select("div.entry").first() match {
      case content: Element => {
        content.select("div:last-of-type").remove()
        content.html()
      }
      case null => ""
    }
  }

}
