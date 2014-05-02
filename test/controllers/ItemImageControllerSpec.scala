package controllers

import org.scalatest._
import org.scalatest.Inside._
import play.api.test._
import play.api.test.Helpers._
import RequestHelper._

/**
 * Start mongo and the authService to execute the tests
 * Mongo: mongod
 * Auth service: run 9001
 */
class ItemImageControllerSpec extends WordSpec with Matchers {

  "ItemImageController" should {

    "get item image" in new WithApplication {
      inside(route(FakeRequest(GET, "/images/item_samsung_galaxy_s5.jpg")
        .withHeaders(authHeader))) {
        case Some(route) =>
          status(route) shouldBe OK
          contentType(route) shouldBe Some("application/json")
          contentAsString(route) should include("image")
      }
    }
  }
}
