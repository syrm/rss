package parsers

import org.jsoup.nodes._
import scala.collection.JavaConversions._
import scala.xml.Node

class LaFermeduweb extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    val site = (item \ "guid").text.split("/")(2)

    page.select("div.contentBillet").first() match {
      case content: Element => {
        for(img <- content.select("img")) {
          if (img.attr("src").matches("^/.+") == true) {
            img.attr("src", "http://" + site + img.attr("src"))
          }
        }

        content.html()
      }
      case null => ""
    }
  }

}
