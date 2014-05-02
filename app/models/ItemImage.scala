package models

import play.api.Play.current
import utils.JsonReadsWrites._
import play.api.libs.json._
import org.bson.types.ObjectId
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

case class ItemImage(id: ObjectId = new ObjectId,
  path: String,
  image: Array[Byte]) extends MongoObject

object ItemImage extends MongoCompanion[ItemImage, ObjectId] {
  // json serialization
  implicit val itemImageFormat = Json.format[ItemImage]

  val dao = new SalatDAO[ItemImage, ObjectId](mongoCollection("item.image")) {}

  def getByPath(path: String): Future[Either[String, ItemImage]] = {
    Future {
      dao.findOne(MongoDBObject("path" -> path)) match {
        case None            => Left(s"Image $path does not exist.")
        case Some(itemImage) => Right(itemImage)
      }
    }
  }
}
