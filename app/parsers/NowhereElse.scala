package parsers

import java.net.URLEncoder
import org.jsoup.nodes._
import scala.util.matching.Regex
import scala.xml.Node

class NowhereElse extends Default {

  override def hasFullContent = true

  override def getContent(item: Node, page: Document) = {
    page.select("div.post-content").first() match {
      case content: Element => {
        val preContent = content.html()
        val videoPlayer = new Regex("(?s)id=\"(jwplayer-[0-9]+)\".+jwplayer\\('\\1'\\).+?file\":\"(.+?)\"", "id", "source")
        videoPlayer.findAllIn(preContent).matchData foreach { m =>
          content.select("div#" + m.group("id")).first().parent().html(s"""
            <div class="videoWrapper">
              <div>
                <object type="application/x-shockwave-flash"
                  data="http://www.nowhereelse.fr/wp-content/uploads/jw-player-plugin-for-wordpress/player/player.swf">
                    <param name="allowfullscreen" value="true">
                    <param name="flashvars" value="file=${URLEncoder.encode(m.group("source"), "ISO8859-1")}">
                </object>
              </div>
            </div>
          """)
        }
        content.html()
      }
      case null => ""
    }
  }

}
