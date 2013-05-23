package parsers

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import scala.xml.Node

class Default extends Parser {

  override def hasFullContent = false

  override def getDate(dateRaw: String): Option[Date] = {
    if (dateRaw == "") {
      return None
    }

    val simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US)

    try {
      Option(new Date(simpleDateFormat.parse(dateRaw).getTime()))
    } catch {
      case e: java.text.ParseException => None
    }
  }

  override def getContent(item: Node, page: Document) = {
    (item \ "description").text
  }

}
