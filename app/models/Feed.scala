package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Feed(
  id:   Pk[Long],
  name: String,
  site: String,
  url:  String,
  favicon: Option[String]) {

  override def toString() = "Feed#" + id
}

object Feed {

  // -- Parsers

  /**
   * Parse a Feed from a ResultSet.
   */
  val simple = {
    get[Pk[Long]]("feed.id") ~
    get[String]("feed.name") ~
    get[String]("feed.site") ~
    get[String]("feed.url") ~
    get[Option[String]]("feed.favicon") map {
        case id ~ name ~ site ~ url ~ favicon => Feed(id, name, site, url, favicon)
      }
  }

  // -- Queries


  /**
   * Retrieve Feed by id.
   */
  def getById(id: Int): Option[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
            where id = {id}
        """).on('id -> id).as(Feed.simple.singleOpt)
    }
  }

  /**
   * Retrieve all Feed.
   */
  def getAll: Seq[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
        """).as(Feed.simple *)
    }
  }

  /**
   * Retrieve all Feed for User.
   */
  def getAllForUser(user: User): Seq[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
            inner join subscription on subscription.feed_id = feed.id
            where subscription.user_id = {userId}
        """).on('userId -> user.id).as(Feed.simple *)
    }
  }

}
