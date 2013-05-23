package parsers

import org.jsoup.nodes._
import scala.xml.Node
import scala.collection.JavaConversions._

class Korben extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    page.select("div.post-content").first() match {
      case content: Element => {
        content.select("div.robots-nocontent").remove()
        content.select("div.ribbon").remove()
        content.select("p:last-of-type").remove()
        content.select("div:last-of-type").remove()
        content.select("br:last-of-type").remove()
        content.select("p:last-of-type:containsOwn(" + 160.toChar +")").remove()

        content.html()
      }
      case null => ""
    }
  }

}
