package models

import play.api.Play.current
import utils.JsonReadsWrites._
import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import org.bson.types.ObjectId
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import mongoContext._
import scala.util.Random
import utils.FileUtils

case class Item(
  id: ObjectId = new ObjectId,
  createdAt: DateTime = DateTime.now,
  endDate: DateTime,
  title: String,
  price: Int,
  bidder: String,
  imagePath: String) extends MongoObject

object Item extends MongoCompanion[Item, ObjectId] {
  // json serialization
  implicit val itemFormat = Json.format[Item]

  val dao = new SalatDAO[Item, ObjectId](mongoCollection("item")) {}

  // Random Date between 5 and 10 min later
  def randomEndDate: DateTime =
    DateTime.now plus Random.nextInt(300000) plus 300000

  // test data
  def macBookPro = Item(
    endDate = randomEndDate,
    title = "MacBook Pro Retina 15",
    price = 1,
    bidder = "B. Daddy",
    imagePath = "item_macbook.jpg")

  def samsungGalaxyS5 = Item(
    endDate = randomEndDate,
    title = "Samsung Galaxy S5",
    price = 1,
    bidder = "Eltan Jon",
    imagePath = "item_samsung_galaxy_s5.jpg")

  def nexus4 = Item(
    endDate = randomEndDate,
    title = "Google Nexus 5",
    price = 1,
    bidder = "Modanna",
    imagePath = "item_nexus_5.JPG")

  def ipadAir = Item(
    endDate = randomEndDate,
    title = "iPad Air 16GB",
    price = 1,
    bidder = "Findsay Bohan",
    imagePath = "item_ipad_air.jpg")

  def chromecast = Item(
    endDate = randomEndDate,
    title = "Google Chromecast",
    price = 1,
    bidder = "Michal Blackson",
    imagePath = "item_chromecast.jpg")
}
