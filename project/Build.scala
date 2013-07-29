import sbt._
import Keys._
import play.Project._


object ApplicationBuild extends Build {

  val appName         = "rss"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "jp.t2v" %% "play2.auth"      % "0.9",
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.4",
    "org.jsoup" % "jsoup" % "1.7.2",
    "mysql" % "mysql-connector-java" % "5.1.20",
    jdbc,
    anorm
  )

  val pbkdf2Project = RootProject(uri("git://github.com/oxman/pbkdf2-scala.git"))

  val main = play.Project(appName, appVersion, appDependencies).settings(
    playOnStarted <+= baseDirectory { root =>
      (serverAddress: java.net.InetSocketAddress) => { Grunt.process = Some(Process("grunt watch").run) }: Unit
    },

    playOnStopped += {
      () => {
        Grunt.process.map(p => p.destroy())
        Grunt.process = None
      }: Unit
    },

    scalacOptions ++= Seq("-feature")
  ).dependsOn(pbkdf2Project)

}

object Grunt {
  var process: Option[Process] = None
}