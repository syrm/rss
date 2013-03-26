package models

import anorm._
import anorm.SqlParser._
import com.github.nremond.PBKDF2
import java.util.Date
import play.api.db._
import play.api.Play.current
import scala.language.postfixOps

case class User(
  id:         Pk[Long],
  name:       String,
  email:      String,
  password:   String,
  salt:       String,
  permission: Permission = Permission.valueOf("NormalUser"),
  date:       Date = new Date
) {
  override def toString() = "User#" + id
}

object User {
  val pbkdf2_iterations = 10000
  val pbkdf2_size = 16

  // -- Parsers

  /**
   * Parse an User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("user.id") ~
    get[String]("user.name") ~
    get[String]("user.email") ~
    get[String]("user.password") ~
    get[String]("user.salt") ~
    get[String]("user.permission") ~
    get[Date]("user.date") map {
      case id~name~email~password~salt~permission~date => User(id, name, email, password, salt, Permission.valueOf(permission), date)
    }
  }

  // -- Queries

  /**
   * Authenticate an User
   */
   def authenticate(name: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select * from user
          where name = {name}
        """).on(
        'name -> name).as(User.simple.singleOpt)
        .filter(user => user.password == PBKDF2(password, user.salt, pbkdf2_iterations, pbkdf2_size))
    }
   }


  /**
   * Retrieve an User from name.
   */
  def getByName(name: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select * from user
          where name = {name}
        """).on(
        'name -> name).as(User.simple.singleOpt)
    }
  }

  /**
   * Create an User.
   */
  def create(user: User): User = {
    DB.withConnection { implicit connection =>
      val id = SQL("""
          insert into user (
              name, email, password, salt, date
            ) values (
              {name}, {email}, {password}, {salt}, {date}
            )
        """).on(
        'name     -> user.name,
        'email    -> user.email,
        'password -> user.password,
        'salt     -> user.salt,
        'date     -> user.date).executeInsert()

      user.copy(id = Id(id.get))
    }
  }

  // -- Utility
  def encryptPassword(password: String, salt: String) =
    PBKDF2(password, salt, pbkdf2_iterations, pbkdf2_size)

}
