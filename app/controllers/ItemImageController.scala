package controllers

import models._
import utils.JsonReadsWrites._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.Logger
import com.wordnik.swagger.annotations._
import javax.ws.rs.PathParam

@Api(value = "/images", description = "Item image operations")
object ItemImageController extends SecuredController {

  @ApiOperation(
    value = "Get item image by name.",
    notes = "Get an item image by file name.",
    nickname = "getImage",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = """Returns the image as Base64 encoded byte array, wrapped in json, e.g. { "image": "bytearray" }"""),
    new ApiResponse(code = 401, message = "Authorization failed due to invalid token."),
    new ApiResponse(code = 401, message = "Authorization failed due to missing token.")))
  def getImage(
    @ApiParam(value = "Image path", required = true, defaultValue = "item_samsung_galaxy_s5.jpg",
      allowMultiple = false)@PathParam("path") path: String) = CrossOriginResource(parse.empty) { request =>
    ItemImage.getByPath(path).map { response =>
      response.fold(
        error => {
          Logger.debug(s"Retrieving the item image $path failed. Error: $error")
          BadRequest(error).as("application/json")
        },
        itemImage => {
          Logger.debug(s"Item image $path successfully retrieved")
          Ok(Json.obj("image" -> itemImage.image))
        }
      )
    }
  }
}
