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
  date:       Date = new Date,
  dateLogin:  Date = new Date,
  token:      Option[String]
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
    get[Date]("user.date") ~
    get[Date]("user.date_login") ~
    get[Option[String]]("user.token") map {
      case id ~ name ~ email ~ password ~ salt ~ permission ~ date ~dateLogin ~ token => User(id, name, email, password, salt, Permission.valueOf(permission), date, dateLogin, token)
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
   * Retrieve an User from token.
   */
  def getByToken(token: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("""
          select * from user
          where token = {token}
        """).on(
        'token -> token).as(User.simple.singleOpt)
    }
  }


  /**
   * Create an User.
   */
  def create(user: User): Option[User] = {
    DB.withConnection { implicit connection =>
      try {
        val id: Option[Long] = SQL("""
            insert into user (
                name, email, password, salt, date, date_login
              ) values (
                {name}, {email}, {password}, {salt}, {date}, {dateLogin}
              )
          """).on(
          'name      -> user.name,
          'email     -> user.email,
          'password  -> user.password,
          'salt      -> user.salt,
          'date      -> user.date,
          'dateLogin -> user.dateLogin).executeInsert()

        user.copy(id = Id(id.get))
        Option(user)
      } catch {
        case e: Throwable => None
      }
    }
  }

  /**
   * Update date login.
   */
  def updateDateLogin(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("update user set date_login = CURRENT_TIMESTAMP where id = {id}").on(
        'id -> id).executeUpdate()
    }
  }

  // -- Utility
  def encryptPassword(password: String, salt: String) =
    PBKDF2(password, salt, pbkdf2_iterations, pbkdf2_size)

}
