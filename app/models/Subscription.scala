package models

import anorm._
import anorm.SqlParser._
import com.mysql.jdbc.exceptions.jdbc4._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Subscription(
  userId: Long,
  feedId: Long,
  date:   Date
) {

  override def toString() = "Subscription#" + userId + "," + feedId
}

object Subscription {

  // -- Parsers

  /**
   * Parse a Subscription from a ResultSet.
   */
  val simple = {
    get[Long]("subscription.user_id") ~
    get[Long]("subscription.feed_id") ~
    get[Date]("subscription.date") map {
      case userId ~ feedId ~ date => Subscription(userId, feedId, date)
    }
  }

  // -- Queries

  /**
   * Create a Subscription.
   */
  def create(subscription: Subscription): Subscription = {
    DB.withConnection { implicit connection =>

      try {
        SQL("""
            insert into subscription
            (user_id, feed_id, date)
            values
            ({userId}, {feedId}, {date})
        """).on(
            'userId -> subscription.userId,
            'feedId -> subscription.feedId,
            'date   -> subscription.date
        ).executeInsert()
      } catch {
        case e: MySQLIntegrityConstraintViolationException => None
      }

      subscription
    }
  }

  /**
   * Delete a Subscription.
   */
  def delete(user: User, feed: Feed) {
    DB.withConnection { implicit connection =>
      SQL("delete from subscription where user_id = {userId} and feed_id = {feedId}").on(
        'userId -> user.id.get,
        'feedId -> feed.id.get
      ).executeUpdate()
    }
  }
}
