package controllers

import jp.t2v.lab.play2.auth.{AuthConfig => play2AuthConfig}
import jp.t2v.lab.play2.auth._
import models._
import play.api.mvc._
import play.api.mvc.Results._
import reflect.classTag

trait AuthConfig extends play2AuthConfig {

  type Id = String
  type User = models.User
  type Authority = Permission

  val idTag = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  override lazy val idContainer: IdContainer[Id] = new CookieIdContainer[Id]

  def resolveUser(name: Id): Option[User] = User.getByName(name)
  def loginSucceeded(request: RequestHeader) = Redirect(routes.Application.index)
  def logoutSucceeded(request: RequestHeader) = Redirect(routes.Auth.login)
  def authenticationFailed(request: RequestHeader) = Redirect(routes.Auth.login)
  def authorizationFailed(request: RequestHeader) = Redirect(routes.Auth.login)

  def authorize(user: User, authority: Authority): Boolean =
    (user.permission, authority) match {
      case (Administrator, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }

}
