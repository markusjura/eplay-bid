package controllers

import play.api.mvc._
import play.api._
import models._
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.iteratee.{ Enumeratee, Concurrent }
import play.api.libs.EventSource
import base.Global
import com.wordnik.swagger.annotations._
import javax.ws.rs.PathParam
import ApiDocumentation._

@Api(value = "/items", description = "Item operations")
object ItemController extends SecuredController {

  val jsonSuccess: JsValue = Json.obj("success" -> true)

  @ApiOperation(
    value = "Re-initialize items.",
    notes = "Use this if all items has been expired. This method will create new items.",
    nickname = "init",
    httpMethod = "POST")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Items has been re-initialized.")))
  def init = Action.async { request =>
    Future {
      Global.initTestData
      Ok(jsonSuccess)
    }
  }

  @ApiOperation(
    value = "Get all non expired items",
    notes = "Returns all items which has not been expired yet as an json array.",
    nickname = "getItems",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Returns items as Json array"),
    new ApiResponse(code = 401, message = "Authorization failed due to invalid token."),
    new ApiResponse(code = 401, message = "Authorization failed due to missing token.")))
  def getItems = SecuredCrossOriginResource(parse.empty) { (username, request) =>
    Logger.debug(s"Start retrieving items..")
    Item.all.map { items =>
      val itemsNotExpired: Seq[Item] = items.filter(_.endDate isAfterNow)
      Logger.debug(s"Items retrieved: ${itemsNotExpired.map(_.title).toList}")
      Ok(Json.toJson(itemsNotExpired))
    }
  }

  // update item error messages
  val errMsgInvalidRequestBody = "Invalid request body."
  val errMsgNoItemFound = "Item not found."
  val errMsgAuctionEnded = "Auction has ended."
  val errMsgBidTooLow = "Bid is not higher as the current bid."

  @ApiOperation(
    value = "Update an item.",
    notes = "Updates an item in the DB. The `bidder` value is set to the username from the token.",
    nickname = "updateItem",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Item which should be updated as json. The bidder is getting ignored." +
      " Instead the value is set by the username from the token.", required = true, dataType = "application/json",
      paramType = "body", defaultValue = updateItemBodyDefaultValue)))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Returns items as Json array"),
    new ApiResponse(code = 401, message = "Authorization failed due to invalid token."),
    new ApiResponse(code = 401, message = "Authorization failed due to missing token.")))
  def updateItem = SecuredCrossOriginResource(parse.json) { (username, request) =>
    Logger.debug(s"Start updating item..")
    Json.fromJson[Item](request.body).fold(
      error => {
        Logger.debug(s"Update item failed. $errMsgInvalidRequestBody")
        Future.successful(BadRequest(Json.obj("error" -> errMsgInvalidRequestBody)))
      },
      item => {
        Item.get(item.id).fold(
          notFound => {
            Logger.debug(s"Update item failed. $errMsgNoItemFound")
            Future.successful(BadRequest(Json.obj("error" -> errMsgNoItemFound)))
          },
          itemInDB => {
            if (itemInDB.endDate isBeforeNow) {
              Logger.debug(s"Update item failed. $errMsgAuctionEnded")
              Future.successful(BadRequest(Json.obj("error" -> errMsgAuctionEnded)))
            } else if (itemInDB.price >= item.price) {
              Logger.debug(s"Update item failed. $errMsgBidTooLow")
              Future.successful(BadRequest(Json.obj("error" -> errMsgBidTooLow)))
            } else {
              Logger.debug(s"Send update to channels.")
              val itemWithUpdatedBidder = item.copy(bidder = username)
              val itemAsJson = Json.toJson(itemWithUpdatedBidder)
              bidChannel.push(itemAsJson)
              Item.update(itemWithUpdatedBidder).map { r =>
                Logger.debug(s"Item successfully updated.")
                Ok(itemAsJson)
              }
            }
          }
        )
      }
    )
  }

  val (bidOut, bidChannel) = Concurrent.broadcast[JsValue]

  /** Enumeratee for detecting disconnect of SSE stream */
  def watchDisconnect(addr: String): Enumeratee[JsValue, JsValue] =
    Enumeratee.onIterateeDone { () => Logger.debug(addr + " - SSE disconnected") }

  @ApiOperation(
    value = "Subscribe to item updates.",
    notes = "Server Sent Events (SSE) channel to subscribe to item updates.",
    nickname = "itemUpdates",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Return items as an `text/event-stream`"),
    new ApiResponse(code = 401, message = "Authorization failed due to invalid token."),
    new ApiResponse(code = 401, message = "Authorization failed due to missing token.")))
  def itemUpdates(
    @ApiParam(value = "Signed token", required = true, defaultValue = "7c798cbad752fd0b0c2fef8aa9f759dbeb1e4e65-1399029405970-user-1",
      allowMultiple = false)@PathParam("token") token: String) = SecuredCrossOriginResource(parse.empty, ParamToken(token)) { (username, request) =>
    Logger.debug(request.remoteAddress + " - SSE connected")
    Future.successful(
      Ok.chunked(
        bidOut
          &> watchDisconnect(request.remoteAddress)
          &> EventSource()
      ).as("text/event-stream"))
  }
}
