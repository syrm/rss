package parsers

import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import org.jsoup.nodes.Document
import scala.xml.Node

class Default extends Parser {

  val formats = Seq(
      "EEE, d MMM yyyy HH:mm:ss z",
      "yyyy-mm-dd'T'HH:mm:ssz",
      "EEE, d MMM yyyy HH:mm:ss Z",
      "d MMM yyyy HH:mm:ss Z",
      "yyyy-mm-dd'T'HH:mm:ss'Z'"
    )

  override def hasFullContent = false

  override def getDate(dateRaw: String): Option[Date] = {
    if (dateRaw == "") {
      return None
    }

    for (format <- formats) {
      val simpleDateFormat = new SimpleDateFormat(format, Locale.US)

      val date = try {
        Option(new Date(simpleDateFormat.parse(dateRaw).getTime()))
      } catch {
        case e: java.text.ParseException => None
      }

      if (date != None) {
        return date
      }
    }

    val dateFormatted = dateRaw.replaceAll("([0-9]{2}):([0-9]{2})$", "$1$2")

    for (format <- formats) {
      val simpleDateFormat = new SimpleDateFormat(format, Locale.US)

      val date = try {
        Option(new Date(simpleDateFormat.parse(dateFormatted).getTime()))
      } catch {
        case e: java.text.ParseException => None
      }

      if (date != None) {
        return date
      }
    }

    None
  }

  override def getContent(item: Node, page: Document) = {
    (item \ "description").text
  }

}
