package controllers

import org.scalatest._
import org.scalatest.Inside._
import play.api.test._
import play.api.test.Helpers._

class AuthControllerSpec extends WordSpec with Matchers {

  "AuthController" should {

    "return OK for any OPTIONS request" in new WithApplication {
      inside(route(FakeRequest("OPTIONS", "/something"))) {
        case Some(authRoute) =>
          status(authRoute) shouldBe OK
      }
    }
  }
}
