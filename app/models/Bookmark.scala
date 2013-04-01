package models

import anorm._
import anorm.SqlParser._
import com.mysql.jdbc.exceptions.jdbc4._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Bookmark(
  userId:  Long,
  itemId:  Long,
  date:    Date
) {

  override def toString() = "Bookmark#" + userId + "," + itemId
}

object Bookmark {

  // -- Parsers

  /**
   * Parse a Bookmark from a ResultSet.
   */
  val simple = {
    get[Long]("bookmark.user_id") ~
    get[Long]("bookmark.item_id") ~
    get[Date]("bookmark.date") map {
      case userId ~ itemId ~ date => Bookmark(userId, itemId, date)
    }
  }

  // -- Queries

  /**
   * Create a Bookmark.
   */
  def create(bookmark: Bookmark): Bookmark = {
    DB.withConnection { implicit connection =>

      try {
        SQL("""
            insert into `bookmark`
            (user_id, item_id, date)
            values
            ({userId}, {itemId}, {date})
        """).on(
            'userId -> bookmark.userId,
            'itemId -> bookmark.itemId,
            'date   -> bookmark.date
        ).executeInsert()
      } catch {
        case e: MySQLIntegrityConstraintViolationException => None
      }

      bookmark
    }
  }

  /**
   * Delete a Bookmark.
   */
  def delete(userId: Long, itemId: Long) {
    DB.withConnection { implicit connection =>
      SQL("delete from bookmark where user_id = {userId} and item_id = {itemId}").on(
        'userId -> userId,
        'itemId -> itemId
      ).executeUpdate()
    }
  }

}
