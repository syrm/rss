package parsers

import java.util.Date
import org.jsoup.nodes.Document
import scala.xml.Node

trait Parser {

  def encoding   = "UTF-8"

  def hasFullContent = true

  def getDate(dateRaw: String): Option[Date]
  def getContent(item: Node, page: Document): String

}

object Parser {

  def apply(name: String): Parser = {
    name match {
      case "Clubic"             => new Clubic
      case "Korben"             => new Korben
      case "Alsacreations"      => new Alsacreations
      case "Ubergizmo"          => new Ubergizmo
      case "WebAppers"          => new WebAppers
      case "Webification"       => new Webification
      case "NowhereElse"        => new NowhereElse
      case "Vincent Abry"       => new VincentAbry
      case "La Ferme du web"    => new LaFermeduweb
      case "Anis Berejeb"       => new AnisBerejeb
      case _                    => new Default
    }
  }

}
