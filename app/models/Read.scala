package models

import anorm._
import anorm.SqlParser._
import com.mysql.jdbc.exceptions.jdbc4._
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class Read(
  userId:  Long,
  itemId:  Long,
  date:    Date
) {

  override def toString() = "Read#" + userId + "," + itemId
}

object Read {

  // -- Parsers

  /**
   * Parse a Read from a ResultSet.
   */
  val simple = {
    get[Long]("read.user_id") ~
    get[Long]("read.item_id") ~
    get[Date]("read.date") map {
      case userId ~ itemId ~ date => Read(userId, itemId, date)
    }
  }

  // -- Queries

  /**
   * Create a Read.
   */
  def create(read: Read): Read = {
    DB.withConnection { implicit connection =>

      try {
        SQL("""
            insert into `read`
            (user_id, item_id, date)
            values
            ({userId}, {itemId}, {date})
        """).on(
            'userId -> read.userId,
            'itemId -> read.itemId,
            'date   -> read.date
        ).executeInsert()
      } catch {
        case e: MySQLIntegrityConstraintViolationException => None
      }

      read
    }
  }

}
