package models

import anorm._
import anorm.SqlParser._
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Item(
  id:      Pk[Long],
  title:   String,
  url:     String,
  content: String,
  date:    Option[Date],
  feedId:  Long,
  guid:    String
) {

  override def toString() = "Item#" + id

  def cleanDate = {
    date match {
      case Some(date) => {
        val simpleFormatHour = new SimpleDateFormat("kms")
        val onlyTime = simpleFormatHour.format(date)

        if (onlyTime == "000" || onlyTime == "100" || onlyTime == "200") {
          val simpleFormat = new SimpleDateFormat("'Le' dd/MM/yyyy")
          simpleFormat.format(date)
        } else {
          val simpleFormat = new SimpleDateFormat("'Le' dd/MM/yyyy 'à' kk'h'mm")
          simpleFormat.format(date)
        }
      }
      case None => "Unknown"
    }
  }
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
    get[Long]("item.feed_id") ~
    get[String]("item.guid") map {
        case id ~ title ~ url ~ content ~ date ~ feedId ~ guid => Item(id, title, url, content, date, feedId, guid)
      }
  }

  val withFeed = Item.simple ~ Feed.simple map {
    case item ~ feed => (item, feed)
  }

  val withFeedAndRead = Item.withFeed ~ (Read.simple ?) map {
    case (item, feed) ~ read => (item, feed, read)
  }

  val withFeedAndReadAndBookmark = Item.withFeedAndRead ~ (Bookmark.simple ?) map {
    case (item, feed, read) ~ bookmark => (item, feed, read, bookmark)
  }

  val withFeedAndUnreadAndBookmark = Item.simple ~ Feed.withUnread ~ (Bookmark.simple ?) map {
    case item ~ feed ~ bookmark => (item, feed, bookmark)
  }

  // -- Queries

  /**
   * Retrieve Item by id.
   */
  def getById(id: Long)(implicit user: User): Option[(Item, Feed, Option[Bookmark])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select *,
            (select count(*) from item
              where item.feed_id = feed.id and not exists (select 1 from `read` where read.item_id = item.id and read.user_id = {userId})) as unread
            from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join bookmark on bookmark.item_id = item.id and bookmark.user_id = {userId}
            where item.id = {id} and subscription.user_id = {userId}
        """).on('id -> id, 'userId -> user.id.get).as(Item.withFeedAndUnreadAndBookmark.singleOpt)
    }
  }

  /**
   * Retrieve all Item for User.
   */
  def getAllForUser(user: User): List[(Item, Feed, Option[Read], Option[Bookmark])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join `read` on read.item_id = item.id and read.user_id = {userId}
            left join bookmark on bookmark.item_id = item.id and bookmark.user_id = {userId}
            where subscription.user_id = {userId}
            order by IF(read.item_id IS NULL, 0, 1), item.date desc
            limit 100
        """).on('userId -> user.id).as(Item.withFeedAndReadAndBookmark *)
    }
  }


  /**
   * Retrieve all searched Item for User.
   */
  def searchAllForUser(user: User, term: String): List[(Item, Feed, Option[Read], Option[Bookmark])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join `read` on read.item_id = item.id and read.user_id = {userId}
            left join bookmark on bookmark.item_id = item.id and bookmark.user_id = {userId}
            where subscription.user_id = {userId} and
              (item.url like {term} or
              item.title like {term} or
              item.content like {term})

            order by IF(read.item_id IS NULL, 0, 1), item.date desc
            limit 100
        """).on('userId -> user.id, 'term -> ("%" + term + "%")).as(Item.withFeedAndReadAndBookmark *)
    }
  }


  /**
   * Retrieve all Item from a Feed for User.
   */
  def getAllFromFeedForUser(feedId: Long, user: User): List[(Item, Feed, Option[Read], Option[Bookmark])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join `read` on read.item_id = item.id and read.user_id = {userId}
            left join bookmark on bookmark.item_id = item.id and bookmark.user_id = {userId}
            where subscription.user_id = {userId} and feed.id = {feedId}
            order by IF(read.item_id IS NULL, 0, 1), item.date desc
            limit 100
        """).on(
        'feedId -> feedId,
        'userId -> user.id).as(Item.withFeedAndReadAndBookmark *)
    }
  }

  /**
   * Retrieve all starred Item for User.
   */
  def getAllStarredForUser(user: User): List[(Item, Feed, Option[Read], Option[Bookmark])] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            inner join feed on feed.id = item.feed_id
            inner join subscription on subscription.feed_id = item.feed_id
            left join `read` on read.item_id = item.id and read.user_id = {userId}
            inner join bookmark on bookmark.item_id = item.id and bookmark.user_id = {userId}
            where subscription.user_id = {userId}
            order by IF(read.item_id IS NULL, 0, 1), item.date desc
            limit 100
        """).on('userId -> user.id).as(Item.withFeedAndReadAndBookmark *)
    }
  }

  /**
   * Retrieve an Item by guid for a Feed.
   */
  def getByGuidForFeed(feedId: Long, guid: String): Option[Item] = {
    DB.withConnection { implicit connection =>
      SQL("""
            select * from item
            where feed_id = {feedId} and guid = md5({guid})
        """).on(
        'feedId -> feedId,
        'guid -> guid).as(Item.simple.singleOpt)
    }
  }

  /**
   * Create or Update an Item.
   */
  def createOrUpdate(item: Item): Item = {
    DB.withConnection { implicit connection =>

      SQL("""
            insert into item
            (id, title, url, content, date, feed_id, guid)
            values
            ({id}, {title}, {url}, {content}, {date}, {feedId}, md5({guid}))
            on duplicate key update title = {title}, content = {content}, date = {date}
        """).on(
        'id      -> item.id,
        'title   -> item.title,
        'url     -> item.url,
        'content -> item.content,
        'date    -> item.date,
        'feedId  -> item.feedId,
        'guid    -> item.guid
      ).executeUpdate()

      item
    }
  }

}
