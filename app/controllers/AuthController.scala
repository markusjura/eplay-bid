package controllers

import play.api.mvc._
import play.api._
import scala.concurrent.Future

object AuthController extends SecuredController {

  def options(url: String) = CrossOriginResource(parse.empty) { request => Future.successful(Ok) }

}
