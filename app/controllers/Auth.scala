package controllers

import anorm._
import jp.t2v.lab.play2.auth._
import models._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc._

object Auth extends Controller with LoginLogout with AuthConfig {

  val loginForm = Form(
    mapping(
      "username" -> text.verifying(nonEmpty),
      "password" -> text.verifying(nonEmpty))(User.authenticate)(_.map(user => (user.name, "")))
    .verifying("Invalid username or password", result => result.isDefined))

  val registerForm = Form(
    tuple(
      "username" -> text.verifying(nonEmpty, maxLength(20)),
      "password" -> text.verifying(nonEmpty, minLength(4)),
      "email"    -> text.verifying(nonEmpty)))

  def login = Action { implicit request =>
    Ok(views.html.auth.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.auth.login(formWithErrors)),
      {
        case (user) => gotoLoginSucceeded(user.get.name)
      })
  }

  def logout = Action { implicit request =>
    gotoLogoutSucceeded
  }

  def register = Action { implicit request =>
    Ok(views.html.auth.register(registerForm))
  }

  def create = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.auth.register(formWithErrors)),
      {
        case (username, password, email) => {
          val salt = java.util.UUID.randomUUID().toString().replaceAll("-", "")
          val passwordCrypted = User.encryptPassword(password, salt)
          User.create(new User(NotAssigned, username, email, passwordCrypted, salt, token = None))
          Redirect(routes.Application.index).withSession(Security.username -> username)
        }
      })
  }

}
