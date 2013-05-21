package parsers

import org.jsoup.nodes._
import org.jsoup.select._
import scala.collection.JavaConversions._
import scala.xml.Node

class AnisBerejeb extends Default {

  override def getContent(item: Node, page: Document) = {
    page.select(".post-content").first() match {
      case content: Element => content.html()
      case null => ""
    }
  }

}
