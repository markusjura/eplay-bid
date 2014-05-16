package controllers

import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WS
import play.api.Play._
import play.api.libs.json.Json
import play.api.Logger

trait SecuredController extends Controller {

  private val corsHeaders = {
    Seq("Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Authorization, api_key",
      // cache access control response for one day
      "Access-Control-Max-Age" -> (60 * 60 * 24).toString)
  }

  private sealed trait VerifyResult
  private case class VerifyError(errorMessage: String) extends VerifyResult
  private case class VerifyOK(userName: String) extends VerifyResult

  /**
   * Verifies the token against the auth service
   * @param token
   * @return VerifyOK with username if token is valid
   *         VerifyError with error message if token is invalid
   */
  private def verify(token: String): Future[VerifyResult] = {
    Logger.debug(s"Start verifiying the token $token against the auth service")
    val authServiceUrl = configuration.getString("service.auth.url").get
    WS.url(s"$authServiceUrl/verify")
      .post(Json.obj("token" -> token))
      .map { response =>
        response.status match {
          case OK =>
            Logger.debug(s"Verify successful.")
            VerifyOK((response.json \ "username").as[String])
          case _ =>
            val error = (response.json \ "error").as[String]
            Logger.debug(s"Verify failed. Error: $error")
            VerifyError(error)
        }
      }
  }

  def CrossOriginResource[A](bp: BodyParser[A] = parse.empty)(f: Request[A] => Future[Result]): Action[A] = {
    Action.async(bp) { request =>
      f(request).map(_.withHeaders(corsHeaders: _*))
    }
  }

  trait TokenSource {
    def unapply(request: Request[_]): Option[String]
  }
  object HeaderToken extends TokenSource {
    def unapply(request: Request[_]): Option[String] = {
      val rawValue: Option[String] = request.headers.get("Authorization")
      val tokenPrefix = "token "
      rawValue.filter(_.startsWith(tokenPrefix)).map { s =>
        val token = s.substring(tokenPrefix.length)
        Logger.debug(s"Retrieved token from Authorization header: $token")
        token
      }
    }
  }
  case class ParamToken(token: String) extends TokenSource {
    def unapply(request: Request[_]): Option[String] = {
      Logger.debug(s"Retrieved token from URI: $token")
      Some(token)
    }
  }

  def SecuredCrossOriginResource[A](bp: BodyParser[A] = parse.empty, ts: TokenSource = HeaderToken)(f: (String, Request[A]) => Future[Result]): Action[A] = {
    CrossOriginResource(bp) { request =>
      Logger.debug(s"Incoming request ${request.uri}")
      ts.unapply(request) match {
        case Some(token) =>
          verify(token) flatMap {
            case VerifyError(errorMessage) =>
              Future.successful(
                Unauthorized(Json.obj("error" -> "Authorization failed due to invalid token.")))
            case VerifyOK(username) =>
              f(username, request)
          }
        case None =>
          Future.successful(
            Unauthorized(Json.obj("error" -> "Authorization failed due to missing token.")))
      }
    }
  }

}
