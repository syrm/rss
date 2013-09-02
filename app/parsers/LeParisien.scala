package parsers

import org.jsoup.nodes._
import scala.util.matching.Regex
import scala.collection.JavaConversions._
import scala.xml.Node

class LeParisien extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    val visuel = page.select(".visuelMain").first() match {
      case content: Element => {
        content.select("h3 strong").remove()

        val h3Text = content.select("h3").text

        if (h3Text.length > 0 && h3Text.substring(h3Text.length - 2, h3Text.length) == " |") {
          content.select("h3").first().text(h3Text.substring(0, h3Text.length - 2))
        }

        content.select("h3").tagName("strong")
        content.select(".pictoDiapo").remove()
        content.html()
      }
      case null => ""
    }

    val diaporama = page.select(".diaporama .vignettes").first() match {
      case content: Element => {
        for(image <- content.select("img")) {
          image.attr("src", "_58x38".r.replaceFirstIn(image.attr("src"), ""))
          image.parent().unwrap()
        }

        content.html()
      }
      case null => ""
    }

    val content = page.select("#content").first() match {
      case content: Element => {
        content.select("btn_noimpr").remove()
        content.select("#pubComplementsArt").remove()
        content.html()
      }
      case null => ""
    }

    val content2 = page.select(".texte-global").first() match {
      case content: Element => {
        content.html()
      }
      case null => ""
    }

    visuel + diaporama + content + content2
  }

}
