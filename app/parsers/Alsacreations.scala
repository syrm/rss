package parsers

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import scala.xml.Node

class Alsacreations extends Default {

  override def dateFormat = "yyyy-mm-dd'T'HH:mm:ssz"

  override def getDate(dateRaw: String): Option[Date] = {
    dateRaw.replaceAll("([0-9]{2}):([0-9]{2})$", "$1$2") match {
      case "" => None
      case dateRaw => {
        val simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US)
        Option(new Date(simpleDateFormat.parse(dateRaw).getTime()))
      }
    }
  }

  override def getContent(item: Node, page: Document) = {
    (item \ "description").text
  }

}
