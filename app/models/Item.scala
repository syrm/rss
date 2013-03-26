package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Item(
  id:      Pk[Long],
  title:   String,
  url:     String,
  content: String,
  date:    Option[Date],
  feedId:  Long
) {

  override def toString() = "Item#" + id
}

object Item {

  // -- Parsers

  /**
   * Parse an Item from a ResultSet.
   */
  val simple = {
    get[Pk[Long]]("item.id") ~
    get[String]("item.title") ~
    get[String]("item.url") ~
    get[String]("item.content") ~
    get[Option[Date]]("item.date") ~
    get[Long]("item.feed_id") map {
        case id ~ title ~ url ~ content ~ date ~ feedId => Item(id, title, url, content, date, feedId)
      }
  }

  val withFeed = Item.simple ~ Feed.simple map {
    case item ~ feed => (item, feed)
  }

  val withFeedAndRead = Item.simple ~ Feed.simple ~ (Read.simple ?) map {
    case item ~ feed ~ read => (item, feed, read)
  }

  // -- Queries

  /**
   * Retrieve all Item for User.
   */
  def getAllForUser(user: User): List[(Item, Feed, Option[Read])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join `read` on read.item_id = item.id and read.user_id = {userId}
            where subscription.user_id = {userId}
            order by IF(read.item_id IS NULL, 0, 1), item.date desc
            limit 30
        """).on('userId -> user.id).as(Item.withFeedAndRead *)
    }
  }

  /**
   * Retrieve an Item from url.
   */
  def getByUrl(url: String): Option[Item] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            where url = {url}
        """).on(
        'url -> url).as(Item.simple.singleOpt)
    }
  }

  /**
   * Create or Update an Item.
   */
  def createOrUpdate(item: Item): Item = {
    DB.withConnection { implicit connection =>

      val id = SQL("""
            insert into item
            (id, title, url, content, date, feed_id)
            values
            ({id}, {title}, {url}, {content}, {date}, {feedId})
            on duplicate key update title = {title}, content = {content}, date = {date}
        """).on(
        'id      -> item.id,
        'title   -> item.title,
        'url     -> item.url,
        'content -> item.content,
        'date    -> item.date,
        'feedId  -> item.feedId
      ).executeUpdate()

      item.copy(id = Id(id))
    }
  }

}
