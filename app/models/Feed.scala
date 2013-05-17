package models

import anorm._
import anorm.SqlParser._
import com.mysql.jdbc.exceptions.jdbc4._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Feed(
  id:   Pk[Long],
  name: String,
  site: String,
  url:  String,
  favicon: Option[String],
  lastUpdate: Option[Date]) {

  var unread: Long = 0

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
    get[Option[String]]("feed.favicon") ~
    get[Option[Date]]("feed.last_update") map {
        case id ~ name ~ site ~ url ~ favicon ~ last_update => Feed(id, name, site, url, favicon, last_update)
      }
  }

  val withUnread = Feed.simple ~ get[Long]("unread") map {
    case feed ~ unread => {
      feed.unread = unread
      feed
    }
  }

  // -- Queries


  /**
   * Retrieve Feed by id.
   */
  def getById(id: Long): Option[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
            where feed.id = {id}
        """).on('id -> id).as(Feed.simple.singleOpt)
    }
  }

  /**
   * Retrieve Feed by id for User.
   */
  def getByIdForUser(id: Long, user: User): Option[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
            inner join subscription on subscription.feed_id = feed.id
            where feed.id = {id} and subscription.user_id = {userId}
        """).on('id -> id, 'userId -> user.id.get).as(Feed.simple.singleOpt)
    }
  }

  /**
   * Retrieve Feed by url.
   */
  def getByUrl(url: String): Option[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from feed
            where url = {url}
        """).on('url -> url).as(Feed.simple.singleOpt)
    }
  }

  /**
   * Retrieve all Feed.
   */
  def getAll: Seq[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select distinct feed.* from feed
            inner join subscription on subscription.feed_id = feed.id
        """).as(Feed.simple *)
    }
  }

  /**
   * Retrieve all Feed for User.
   */
  def getAllForUser(user: User): Seq[Feed] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select feed.*,
            (select count(*) from item
              where item.feed_id = feed.id and not exists (select 1 from `read` where read.item_id = item.id and read.user_id = {userId})) as unread
            from feed
            inner join subscription on subscription.feed_id = feed.id
            where subscription.user_id = {userId}
        """).on('userId -> user.id).as(Feed.withUnread *)
    }
  }

  /**
   * Create a Feed.
   */
  def create(feed: Feed): Feed = {
    DB.withConnection { implicit connection =>
      val id: Option[Long] = try {
        SQL("""
            insert into feed
            (id, name, site, url, favicon)
            values
            ({id}, {name}, {site}, {url}, {favicon})
          """).on(
          'id      -> feed.id,
          'name    -> feed.name,
          'site    -> feed.site,
          'url     -> feed.url,
          'favicon -> feed.favicon
        ).executeInsert()
      } catch {
        case e: MySQLIntegrityConstraintViolationException => Option(this.getByUrl(feed.url).get.id.get)
      }

      feed.copy(id = Id(id.get))
    }
  }

  /**
   * Update last update.
   */
  def updateLastUpdate(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("update feed set last_update = CURRENT_TIMESTAMP where id = {id}").on(
        'id -> id).executeUpdate()
    }
  }

  /**
   * Update last error.
   */
  def updateLastError(id: Long, error: String) {
    DB.withConnection { implicit connection =>
      SQL("update feed set last_error = {error} where id = {id}").on(
        'id -> id, 'error -> error).executeUpdate()
    }
  }

}
