package base

import java.io.IOException
import models._
import models.Item._
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Results._
import play.api._
import play.Configuration
import com.mongodb.casbah.{ MongoURI, MongoConnection }
import com.mongodb.casbah.commons.conversions.scala._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.Akka
import scala.concurrent.duration._
import utils.FileUtils

object Global extends GlobalSettings {

  /**
   * Called on application start
   */
  override def onStart(app: Application) {
    // Connection to DB..
    val conf = Play.application.configuration
    val uri = MongoURI(conf.getString("mongodb.default.uri").get)
    val connection = MongoConnection(uri)
    val db = connection(conf.getString("mongodb.default.db").get)

    // Ensure indexes - collections and columns
    db("item").ensureIndex("title")
    db("item.image").ensureIndex("path")

    // Register Joda time conversion..
    RegisterJodaTimeConversionHelpers()
    RegisterConversionHelpers()

    // Initialize DB every 10 minutes
    Akka.system.scheduler.schedule(0.milliseconds, 10.minutes)(initTestData)

    // Application started..
  }

  def initTestData = {
    Logger.debug("Initialize data..")

    // remove all items
    Item.deleteAll
    ItemImage.deleteAll

    // add items
    // mac book
    val macBook = Item.addGet(macBookPro)
    addImage(macBook.imagePath)
    // s5
    val s5 = Item.addGet(samsungGalaxyS5)
    addImage(s5.imagePath)
    // nexus 4
    val n4 = Item.addGet(nexus4)
    addImage(n4.imagePath)
    // ipad air
    val ipad = Item.addGet(ipadAir)
    addImage(ipad.imagePath)
    // chromecast
    val cc = Item.addGet(chromecast)
    addImage(cc.imagePath)
  }

  private def addImage(path: String): Unit = {
    val resourcePath = s"public/images/$path"
    val imageBytes = FileUtils.fromFile(resourcePath).getOrElse(throw new IOException(s"No resource at path: $resourcePath"))
    val itemImage = ItemImage(path = path, image = imageBytes)
    ItemImage.add(itemImage)
  }
}