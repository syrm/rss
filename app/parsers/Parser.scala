package parsers

import java.util.Date
import org.jsoup.nodes.Document
import scala.xml.Node

trait Parser {

  def dateFormat = "EEE, d MMM yyyy HH:mm:ss z"
  def encoding   = "UTF-8"

  def getDate(item: Node): Option[Date]
  def getContent(item: Node, page: Document): String

}

object Parser {

  def apply(name: String): Parser = {
    name match {
      case "Clubic"        => new Clubic
      case "Korben"        => new Korben
      case "Alsacreations" => new Alsacreations
      case "Ubergizmo"     => new Ubergizmo
      case _               => new Default
    }
  }

}
