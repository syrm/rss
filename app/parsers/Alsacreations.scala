package parsers

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import scala.xml.Node

class Alsacreations extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    (item \ "description").text
  }

}
