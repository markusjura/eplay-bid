package controllers

import org.scalatest._
import org.scalatest.Inside._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import models.Item
import play.api.libs.concurrent.Execution.Implicits._
import org.joda.time.DateTime
import RequestHelper._

/**
 * Start mongo and the authService to execute the tests
 * Mongo: mongod
 * Auth service: run 9001
 */
class ItemControllerSpec extends WordSpec with Matchers {

  "ItemController" should {

    "initialize test data" in new WithApplication {
      inside(route(FakeRequest(POST, "/init"))) {
        case Some(authRoute) =>
          status(authRoute) shouldBe OK
      }
    }

    "get items" in new WithApplication {
      val r = route(FakeRequest(GET, "/items").withHeaders(authHeader))
      inside(r) {
        case Some(route) =>
          status(route) shouldBe OK
          contentType(route) shouldBe Some("application/json")
          val items: Seq[Item] = Json.fromJson[Seq[Item]](contentAsJson(route)).get
          items should have size 5
      }
    }

    "not retrieve items if request is invalid" in new WithApplication {
      inside(route(FakeRequest(GET, "/items"))) {
        case Some(route) =>
          status(route) shouldBe UNAUTHORIZED
      }
    }

    "update an item" in new WithApplication {
      val item = Item.all.map { items =>
        val oldItem = items.head
        val newItem = oldItem.copy(title = "New title")

        inside(route(FakeRequest(POST, "/items").withHeaders(authHeader)
          .withBody(Json.toJson(newItem)))) {
          case Some(route) =>
            status(route) shouldBe OK
            contentType(route) shouldBe Some("application/json")
            inside(Item.get(oldItem.id)) {
              case Right(item) => newItem shouldBe item
            }
        }
      }
    }

    "not update an item: Invalid request body" in new WithApplication {
      inside(route(FakeRequest(POST, "/items").withHeaders(authHeader)
        .withBody(Json.obj("text" -> "invalid json")))) {
        case Some(route) =>
          status(route) shouldBe BAD_REQUEST
          contentType(route) shouldBe Some("application/json")
          contentAsJson(route) shouldBe Json.obj("error" -> "Invalid request body.")
      }
    }

    "not update an item: Item not found" in new WithApplication {
      val itemNotFound = Item.ipadAir // different id

      inside(route(FakeRequest(POST, "/items").withHeaders(authHeader)
        .withBody(Json.toJson(itemNotFound)))) {
        case Some(route) =>
          status(route) shouldBe BAD_REQUEST
          contentType(route) shouldBe Some("application/json")
          contentAsJson(route) shouldBe Json.obj("error" -> "Item not found.")
      }
    }

    "not update an item: Auction has ended" in new WithApplication {
      val item = Item.all.map { items =>
        val expiredItem = items.head.copy(endDate = DateTime.now)
        Item.update(expiredItem)

        inside(route(FakeRequest(POST, "/items").withHeaders(authHeader)
          .withBody(Json.toJson(expiredItem)))) {
          case Some(route) =>
            status(route) shouldBe BAD_REQUEST
            contentType(route) shouldBe Some("application/json")
            contentAsJson(route) shouldBe Json.obj("error" -> "Auction has ended.")
        }
      }
    }

    "not update an item: Bid is too low" in new WithApplication {
      val item = Item.all.map { items =>
        val itemBidTooLow = items.head.copy(price = 0)

        inside(route(FakeRequest(POST, "/items").withHeaders(authHeader)
          .withBody(Json.toJson(itemBidTooLow)))) {
          case Some(route) =>
            status(route) shouldBe BAD_REQUEST
            contentType(route) shouldBe Some("application/json")
            contentAsJson(route) shouldBe Json.obj("error" -> "Bid is not higher as the current bid.")
        }
      }
    }
  }
}
