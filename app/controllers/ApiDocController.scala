package controllers

import play.api.mvc._

object ApiDocController extends Controller {

  def view = Action { request =>
    Ok(views.html.index(s"http://${request.host}/api"))
  }
}
