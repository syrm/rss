package parsers

import org.jsoup.nodes._
import scala.util.matching.Regex
import scala.collection.JavaConversions._
import scala.xml.Node

class LeFigaro extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    page.select(".fig-main-col").first() match {
      case content: Element => {
        content.select(".fig-side-col").remove()
        content.select(".fig-ad").remove()
        content.select(".fig-adgps").remove()
        content.select(".fig-embed-vid img").remove()

        for(video <- content.select("object")) {
          video.removeAttr("height").removeAttr("width")
          video.wrap("<div class='videoWrapper'></div>")
        }

        val videoPlayer = new Regex("data-videoplayer=\"([0-9]+)\" data-playerid=\"([0-9]+)\" data-playerkey=\"([^\"]+)\" data-objectid=\"([^\"]+)\"",
            "videoplayer", "playerid", "playerkey", "objectid")

        videoPlayer.findAllIn(content.html()).matchData foreach { m =>
          content.select("div[data-playerid=" + m.group("playerid") + "]").first().html(s"""
            <div class="videoWrapper">
              <object type="application/x-shockwave-flash" id="${m.group("objectid")}"
                data="http://c.brightcove.com/services/viewer/federated_f9?flashID=${m.group("objectid")}&amp;playerID=${m.group("playerid")}&amp;playerKey=${m.group("playerkey")}&amp;isVid=true&amp;isUI=true&amp;htmlFallback=true&amp;dynamicStreaming=true&amp;%40videoPlayer=${m.group("videoplayer")}">
                  <param name="allowfullscreen" value="true">
                  <param name="quality" value="high">
              </object>
            </div>
          """)

        }

        content.html()
      }
      case null => ""
    }
  }

}
