package models

import com.mongodb.casbah.Imports._
import mongoContext._
import com.novus.salat.dao._
import play.api.Play.current
import com.mongodb.casbah.commons.MongoDBObject
import scala.reflect.ClassTag
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

/**
 * Trait for the companion object of an entity object
 */
trait MongoCompanion[M <: MongoObject, O <: ObjectId] extends ModelCompanion[M, ObjectId] {
  /**
   * Type T is a class tag of M, which extends from MongoObject.
   * Type T i needed to work with the Caching framework. The caching framework is based on Java.
   * Java doesn't understand the construct of lower view bounds (M <: MongoObject).
   * Instead Java need to get the instace as a classTag. In the trait T will be converted as M again.
   * In other words: T need to be used if data from the Cache is retrieved.
   * T should only be used internally and should never be returned to the outside.
   */
  type T = ClassTag[M]

  // dao to access salat api
  val dao: SalatDAO[M, ObjectId]

  /**
   * Delete an entry by id
   */
  def delete(id: ObjectId): Unit =
    dao.removeById(id)

  /**
   * Delete multiple entries by id
   */
  def delete(ids: Seq[ObjectId]): Unit = ids.foreach(delete(_))

  /**
   * Delete an entry by object
   */
  def delete(m: M): Unit = {
    dao.remove(m)
  }

  def deleteAll: Unit = {
    all.map { items =>
      delete(items.map(_.id))
    }
  }

  /**
   * Get an entry by id
   */
  def get(id: ObjectId): Either[String, M] =
    dao.findOneById(id) match {
      case None    => Left("Entry with id " + id.toString + " in " + dao.collection.name + " doesn't exist.")
      case Some(m) => Right(m.asInstanceOf[M].sort.asInstanceOf[M])
    }

  /**
   * Get multiple entries by id.
   * If not all entries are in cache => get all values from DB
   */
  def get(ids: Seq[ObjectId]): Seq[M] =
    dao.find("_id" $in ids).toSeq

  /**
   * Add an entry by object
   * No write concern => If unique key has been violated the entry will not be added but the new ObjectId will be returned
   */
  def add(m: M): ObjectId =
    dao.insert(m).get

  /**
   * Add multiple entries by object
   */
  def add(ms: Seq[M]): Seq[ObjectId] = ms.map(add(_))

  /**
   * Add an entry by object and return object newly created object
   */
  def addGet(m: M): M = {
    val id = add(m)
    val dbM = dao.findOneById(id).get.sort.asInstanceOf[M]
    dbM
  }

  /**
   * Add multiple entries by object and return newly created objects
   */
  def addGet(ms: Seq[M]): Seq[M] = ms.map(addGet(_))

  /**
   * Update an entry
   */
  def update(m: M): Future[Unit] =
    Future(dao.update(MongoDBObject("_id" -> m.id), m, false, false, dao.collection.writeConcern))

  /**
   * Get all element
   */
  def all: Future[Seq[M]] = Future {
    findAll.toSeq.map(_.sort.asInstanceOf[M])
  }

  /**
   * Count all elements
   */
  def count: Int = findAll.size
}

/**
 * Trait for the case class (entity object).
 * In each entity object an id of type ObjectId need to be defined
 */
trait MongoObject {
  val id: ObjectId
  def sort: MongoObject = this
}