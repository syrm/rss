package parsers

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import scala.xml.Node

class Default extends Parser {

  override def getDate(item: Node): Option[Date] = {
    val dateRaw = (item \ "pubDate").text

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
