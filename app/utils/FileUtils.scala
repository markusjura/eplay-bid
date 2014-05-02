package utils

import org.apache.commons.io.IOUtils
import play.api._

object FileUtils {

  def fromFile(path: String)(implicit application: Application): Option[Array[Byte]] = {
    Play.resourceAsStream(path).map(stream => IOUtils.toByteArray(stream))
  }
}
