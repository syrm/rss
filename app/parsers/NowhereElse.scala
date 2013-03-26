package parsers

import org.jsoup.nodes._
import scala.xml.Node

class NowhereElse extends Default {

  override def getContent(item: Node, page: Document) = {
    page.select("div.post-content").first() match {
      case content: Element => content.html()
      case null => ""
    }
  }

}
