import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import scala.concurrent.duration._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    syncFeed
  }

  def syncFeed {
    Akka.system.scheduler.schedule(0.seconds, 1.minutes) {
      controllers.Process.all
    }
  }

}
