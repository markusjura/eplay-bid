package utils

import scala.io.Source
import org.bson.types.ObjectId
import javax.activation.MimetypesFileTypeMap
import java.io.InputStream
import java.io.ByteArrayInputStream
import play.api.mvc.MultipartFormData
import play.api.libs.Files

object Conversions {
  /**
   * String / ObjectId
   */
  implicit def objectIdToString(objectId: ObjectId): String = objectId.toString
  implicit def stringToObjectId(string: String): ObjectId = new ObjectId(string)

  /**
   * Files
   */
  implicit def JavaFileToByteArray(file: java.io.File): Array[Byte] = {
    val source = Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = source.map(_.toByte).toArray
    source.close()
    byteArray
  }

  implicit def ByteArrayToInputStream(byteArray: Array[Byte]): InputStream = new ByteArrayInputStream(byteArray)
}